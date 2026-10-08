package es.uclm.esibuy.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Configuración de Spring Data MongoDB común a todos los módulos.
 *
 * <p>Sustituye el convertidor por defecto de Spring Boot por uno que no guarda el campo
 * {@code _class} (el nombre interno de la clase Java) en cada documento: no forma parte del
 * diseño de la base de datos y revelaría la estructura del código. Solo haría falta si hubiera
 * herencia entre documentos, y no la hay.
 */
@Configuration
@EnableMongoAuditing // rellena @CreatedDate y @LastModifiedDate (createdAt y updatedAt del documento de BBDD)
public class MongoConfig {

    @Bean
    MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory,
                                                MongoMappingContext context,
                                                MongoCustomConversions conversions) {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), context);
        converter.setCustomConversions(conversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
