package es.uclm.esibuy.backend.productos.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.AlertasFavoritosPort;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.alertas.AlertasFavoritosAdapter;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.SupervisionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.port.out.ImagenStoragePort;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.application.service.CatalogoProductosService;
import es.uclm.esibuy.backend.productos.application.service.ExistenciasProductosService;
import es.uclm.esibuy.backend.productos.application.service.ConteoProductosService;
import es.uclm.esibuy.backend.productos.application.service.GestionProductosService;
import es.uclm.esibuy.backend.productos.application.service.GestionImagenesProductoService;
import es.uclm.esibuy.backend.productos.infrastructure.imagen.ValidadorImagen;
import es.uclm.esibuy.backend.productos.application.service.GestionProductosVendedorService;
import es.uclm.esibuy.backend.productos.application.service.InventarioVendedorService;
import es.uclm.esibuy.backend.productos.application.service.ListadoProductosService;
import es.uclm.esibuy.backend.productos.application.service.SupervisionProductosService;
import es.uclm.esibuy.backend.productos.application.service.ValoracionesProductoService;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus.VirusTotalAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.imagen.GridFsImagenAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoDocumentMapper;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoRepository;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios.UsuarioEnSesionAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios.VendedorAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring del módulo de productos: conecta cada puerto con su adaptador, como
 * UsuariosConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class ProductosConfig {

    private static final String URL_VIRUSTOTAL = "https://www.virustotal.com/api/v3";
    private static final Duration TIEMPO_MAXIMO_VIRUSTOTAL = Duration.ofSeconds(5);
    // Se mantienen 2 consultas para limitar las peticiones a VirusTotal (1 subida + 2 consultas por archivo).
    // Se espacian 15 s porque la API puede mantener el análisis en cola durante varios segundos.
    private static final int CONSULTAS_ANALISIS_VIRUSTOTAL = 2;
    private static final Duration ESPERA_ENTRE_CONSULTAS_VIRUSTOTAL = Duration.ofSeconds(15);

    @Bean
    ImagenStoragePort imagenStoragePort(GridFsTemplate gridFsTemplate) {
        return new GridFsImagenAdapter(gridFsTemplate);
    }

    @Bean
    ProductoRepositoryPort productoRepositoryPort(ProductoMongoRepository productoMongoRepository,
            MongoTemplate mongoTemplate) {
        return new ProductoMongoAdapter(productoMongoRepository, new ProductoDocumentMapper(), mongoTemplate);
    }

    @Bean
    UsuarioEnSesionPort usuarioEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new UsuarioEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    ListadoProductosUseCase listadoProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            UsuarioEnSesionPort usuarioEnSesionPort) {
        return new ListadoProductosService(productoRepositoryPort, usuarioEnSesionPort);
    }

    @Bean
    VendedorPort vendedorPort(AdministracionUsuariosUseCase administracionUsuariosUseCase) {
        return new VendedorAdapter(administracionUsuariosUseCase);
    }

    @Bean
    SupervisionProductosUseCase supervisionProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            VendedorPort vendedorPort) {
        return new SupervisionProductosService(productoRepositoryPort, vendedorPort);
    }

    // Alertas de los productos favoritos (AB#529): el módulo de alertas decide a quién avisar de cada cambio
    @Bean
    AlertasFavoritosPort productosAlertasPort(GeneracionAlertasUseCase generacionAlertasUseCase) {
        return new AlertasFavoritosAdapter(generacionAlertasUseCase);
    }

    @Bean
    GestionProductosUseCase gestionProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            CategoriaRepositoryPort categoriaRepositoryPort, UsuarioEnSesionPort usuarioEnSesionPort,
            AnalisisImagenPort analisisImagenPort, AlertasFavoritosPort productosAlertasPort) {
        return new GestionProductosService(productoRepositoryPort, categoriaRepositoryPort, usuarioEnSesionPort,
                analisisImagenPort, productosAlertasPort);
    }

    @Bean
    GestionImagenesProductoService gestionImagenesProductoService(ProductoRepositoryPort productos,
            ImagenStoragePort almacenamiento, AnalisisImagenPort antivirus, UsuarioEnSesionPort usuarioEnSesion) {
        return new GestionImagenesProductoService(productos, almacenamiento, antivirus,
                new ValidadorImagen(), usuarioEnSesion);
    }

    @Bean
    GestionProductosVendedorUseCase gestionProductosVendedorUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new GestionProductosVendedorService(productoRepositoryPort);
    }

    @Bean
    ExistenciasProductosUseCase existenciasProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            AlertasFavoritosPort productosAlertasPort) {
        return new ExistenciasProductosService(productoRepositoryPort, productosAlertasPort);
    }

    // HU13: el módulo de valoraciones deja aquí la media y el número de valoraciones de cada producto
    @Bean
    ValoracionesProductoUseCase valoracionesProductoUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new ValoracionesProductoService(productoRepositoryPort);
    }

    @Bean
    CatalogoProductosUseCase catalogoProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            VendedorPort vendedorPort, UsuarioEnSesionPort usuarioEnSesionPort) {
        return new CatalogoProductosService(productoRepositoryPort, vendedorPort, usuarioEnSesionPort);
    }

    @Bean
    ConteoProductosUseCase conteoProductosUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new ConteoProductosService(productoRepositoryPort);
    }

    // HU17: el módulo de estadísticas pide aquí los productos del vendedor
    @Bean
    InventarioVendedorUseCase inventarioVendedorUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new InventarioVendedorService(productoRepositoryPort);
    }

    // Análisis antivirus de la URL de las imágenes con VirusTotal (HU31, T397). La API key sale de la variable de
    // entorno VIRUSTOTAL_API_KEY: nunca en el repositorio. Cada petición espera como mucho 5 s, como el SMTP; si se
    // pasa, el adaptador lo trata como «sin resultado».
    @Bean
    AnalisisImagenPort analisisImagenPort(@Value("${esibuy.virustotal.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory conexion = new SimpleClientHttpRequestFactory();
        conexion.setConnectTimeout(TIEMPO_MAXIMO_VIRUSTOTAL);
        conexion.setReadTimeout(TIEMPO_MAXIMO_VIRUSTOTAL);
        RestClient virusTotal = RestClient.builder()
                .baseUrl(URL_VIRUSTOTAL)
                .defaultHeader("x-apikey", apiKey)
                .requestFactory(conexion)
                .build();
        return new VirusTotalAdapter(virusTotal, CONSULTAS_ANALISIS_VIRUSTOTAL, ESPERA_ENTRE_CONSULTAS_VIRUSTOTAL);
    }
}
