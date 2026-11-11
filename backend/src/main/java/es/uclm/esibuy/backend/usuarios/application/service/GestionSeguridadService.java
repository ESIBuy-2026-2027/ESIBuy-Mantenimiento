
package es.uclm.esibuy.backend.usuarios.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.usuarios.application.port.in.ActualizarFactoresCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.ConfiguracionSeguridadResponse;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionSeguridadUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class GestionSeguridadService implements GestionSeguridadUseCase {

        private final UsuarioRepositoryPort usuarioRepository;
        private final FactorAutenticacionRepositoryPort factorRepository;

        public GestionSeguridadService(
                        UsuarioRepositoryPort usuarioRepository,
                        FactorAutenticacionRepositoryPort factorRepository) {
                this.usuarioRepository = usuarioRepository;
                this.factorRepository = factorRepository;
        }

        @PreAuthorize(Permisos.AUTENTICADO_PROPIO)
        @Override
        public ConfiguracionSeguridadResponse consultar(String email) {
                Usuario usuario = buscarUsuario(email);

                FactorAutenticacion factor = factorRepository
                                .buscarPorUsuario(usuario.getId())
                                .orElse(null);

                boolean configurado = factor != null && factor.estaConfirmado();

                return new ConfiguracionSeguridadResponse(
                                usuario.isEmailOtpEnabled(),
                                usuario.isAuthenticatorEnabled(),
                                configurado,
                                configurado ? factor.getVerificadoEn() : null);
        }

        @PreAuthorize(Permisos.AUTENTICADO_PROPIO)
        @Override
        public ConfiguracionSeguridadResponse actualizar(
                        String email,
                        ActualizarFactoresCommand comando) {

                Usuario usuario = buscarUsuario(email);

                FactorAutenticacion factor = factorRepository
                                .buscarPorUsuario(usuario.getId())
                                .orElse(null);

                boolean authenticatorConfigurado = factor != null && factor.estaConfirmado();

                if (comando.authenticatorEnabled() && !authenticatorConfigurado) {
                        throw new UsuarioInvalidoException(
                                        "Debes configurar y confirmar Microsoft Authenticator antes de activarlo");
                }

                usuario.actualizarPreferenciasSeguridad(
                                comando.emailOtpEnabled(),
                                comando.authenticatorEnabled());

                usuarioRepository.guardar(usuario);

                return new ConfiguracionSeguridadResponse(
                                usuario.isEmailOtpEnabled(),
                                usuario.isAuthenticatorEnabled(),
                                authenticatorConfigurado,
                                authenticatorConfigurado ? factor.getVerificadoEn() : null);
        }

        private Usuario buscarUsuario(String email) {
                return usuarioRepository.buscarPorCorreo(email)
                                .orElseThrow(() -> new CredencialesInvalidasException(
                                                "Credenciales inválidas"));
        }
}
