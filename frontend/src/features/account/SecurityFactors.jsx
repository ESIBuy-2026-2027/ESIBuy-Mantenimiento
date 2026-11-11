import { useCallback, useId, useState } from 'react'
import {
    Button,
    Card,
    Checkbox,
    Input,
    MessageBar,
    MessageBarActions,
    MessageBarBody,
    Spinner,
    Text,
    Title3,
    makeStyles,
    tokens,
} from '@fluentui/react-components'
import {
    enrollSecurityAuthenticator,
    getSecurityFactors,
    updateSecurityFactors,
    verifySecurityAuthenticator,
} from '../../api/security'
import { useApiQuery } from '../../hooks/useApiQuery'

const useStyles = makeStyles({
    card: {
        display: 'flex',
        flexDirection: 'column',
        gap: tokens.spacingVerticalL,
        padding: tokens.spacingVerticalXL,
    },
    description: {
        color: tokens.colorNeutralForeground3,
    },
    options: {
        display: 'flex',
        flexDirection: 'column',
        gap: tokens.spacingVerticalL,
    },
    option: {
        display: 'flex',
        flexDirection: 'column',
        gap: tokens.spacingVerticalXS,
    },
    actions: {
        display: 'flex',
        alignItems: 'center',
        gap: tokens.spacingHorizontalM,
        flexWrap: 'wrap',
    },
    enrollment: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        gap: tokens.spacingVerticalM,
        padding: tokens.spacingVerticalL,
        border: `1px solid ${tokens.colorNeutralStroke1}`,
        borderRadius: tokens.borderRadiusMedium,
    },
    qr: {
        width: '200px',
        height: '200px',
        objectFit: 'contain',
    },
    code: {
        width: '220px',
    },
})

export default function SecurityFactors() {
    const styles = useStyles()
    const titleId = useId()
    const fetcher = useCallback(() => getSecurityFactors(), [])
    const { data, failed, retry } = useApiQuery(fetcher)

    const [cambios, setCambios] = useState(null)
    const [configuracionGuardada, setConfiguracionGuardada] = useState(null)
    const [guardando, setGuardando] = useState(false)
    const [iniciandoAlta, setIniciandoAlta] = useState(false)
    const [verificandoAlta, setVerificandoAlta] = useState(false)
    const [qr, setQr] = useState(null)
    const [codigo, setCodigo] = useState('')
    const [aviso, setAviso] = useState(null)

    const base = configuracionGuardada ?? data
    const configuracion = cambios ?? base

    const hayCambios = Boolean(
        cambios &&
        base &&
        (
            cambios.emailOtpEnabled !== base.emailOtpEnabled ||
            cambios.authenticatorEnabled !== base.authenticatorEnabled
        ),
    )

    const cambiar = (campo, valor) => {
        setCambios((actuales) => ({
            ...(actuales ?? base),
            [campo]: valor,
        }))
        setAviso(null)
    }

    const guardar = async () => {
        if (!configuracion) return

        setGuardando(true)
        setAviso(null)

        try {
            const actualizada = await updateSecurityFactors({
                emailOtpEnabled: configuracion.emailOtpEnabled,
                authenticatorEnabled: configuracion.authenticatorEnabled,
            })
            setConfiguracionGuardada(actualizada)
            setCambios(null)
            setAviso({ intent: 'success', texto: 'La configuración de seguridad se ha guardado.' })
        } catch {
            setAviso({
                intent: 'error',
                texto: 'No se ha podido guardar la configuración de seguridad. Inténtalo de nuevo.',
            })
        } finally {
            setGuardando(false)
        }
    }

    const iniciarAlta = async () => {
        setIniciandoAlta(true)
        setAviso(null)
        try {
            const resultado = await enrollSecurityAuthenticator()
            setQr(resultado.qr)
            setCodigo('')
        } catch {
            setAviso({
                intent: 'error',
                texto: 'No se ha podido generar el código QR de Microsoft Authenticator. Inténtalo de nuevo.',
            })
        } finally {
            setIniciandoAlta(false)
        }
    }

    const confirmarAlta = async () => {
        if (!/^\d{6}$/.test(codigo)) {
            setAviso({ intent: 'error', texto: 'Introduce el código de 6 dígitos de la aplicación.' })
            return
        }

        setVerificandoAlta(true)
        setAviso(null)
        try {
            await verifySecurityAuthenticator(codigo)
            const actualizada = await getSecurityFactors()
            setConfiguracionGuardada(actualizada)
            setCambios(null)
            setQr(null)
            setCodigo('')
            setAviso({
                intent: 'success',
                texto: 'Microsoft Authenticator se ha configurado y verificado. Ahora puedes activarlo y guardar los cambios.',
            })
        } catch {
            setAviso({
                intent: 'error',
                texto: 'El código no es válido o no se ha podido confirmar la configuración. Comprueba el código e inténtalo de nuevo.',
            })
        } finally {
            setVerificandoAlta(false)
        }
    }

    const descartar = () => {
        setCambios(null)
        setAviso(null)
    }

    if (!data && !failed) {
        return <Spinner label="Cargando configuración de seguridad…" />
    }

    return (
        <Card className={styles.card} role="region" aria-labelledby={titleId}>
            <Title3 as="h2" id={titleId}>
                Seguridad de la cuenta
            </Title3>

            <Text className={styles.description}>
                Configura los métodos adicionales de autenticación que quieres utilizar al iniciar sesión.
            </Text>

            {aviso && (
                <MessageBar intent={aviso.intent}>
                    <MessageBarBody>{aviso.texto}</MessageBarBody>
                </MessageBar>
            )}

            {failed ? (
                <MessageBar intent="error">
                    <MessageBarBody>
                        No se ha podido cargar la configuración de seguridad.
                    </MessageBarBody>
                    <MessageBarActions>
                        <Button onClick={retry}>Reintentar</Button>
                    </MessageBarActions>
                </MessageBar>
            ) : (
                <>
                    <div className={styles.options}>
                        <div className={styles.option}>
                            <Checkbox
                                label="Código de verificación por correo electrónico"
                                checked={configuracion?.emailOtpEnabled === true}
                                disabled={guardando || iniciandoAlta || verificandoAlta}
                                onChange={(_event, option) => cambiar('emailOtpEnabled', option.checked === true)}
                            />
                            <Text className={styles.description}>
                                Solicita un código enviado a tu correo electrónico como paso adicional de autenticación.
                            </Text>
                        </div>

                        <div className={styles.option}>
                            <Checkbox
                                label="Microsoft Authenticator"
                                checked={configuracion?.authenticatorEnabled === true}
                                disabled={
                                    guardando ||
                                    iniciandoAlta ||
                                    verificandoAlta ||
                                    configuracion?.authenticatorConfigured !== true
                                }
                                onChange={(_event, option) => cambiar('authenticatorEnabled', option.checked === true)}
                            />
                            <Text className={styles.description}>
                                Utiliza códigos temporales generados por Microsoft Authenticator.
                            </Text>

                            {configuracion?.authenticatorConfigured !== true && !qr && (
                                <>
                                    <Text className={styles.description}>
                                        Todavía no has configurado Microsoft Authenticator. Puedes iniciar el alta aquí y escanear el QR con la aplicación.
                                    </Text>
                                    <div className={styles.actions}>
                                        <Button
                                            appearance="secondary"
                                            disabled={iniciandoAlta || guardando}
                                            onClick={iniciarAlta}
                                        >
                                            {iniciandoAlta ? 'Generando QR…' : 'Configurar Microsoft Authenticator'}
                                        </Button>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>

                    {qr && (
                        <div className={styles.enrollment}>
                            <Title3 as="h3">Configurar Microsoft Authenticator</Title3>
                            <Text>
                                Abre Microsoft Authenticator, añade una cuenta y escanea este código QR.
                                Después introduce el código de 6 dígitos que aparece en la aplicación.
                            </Text>
                            <img
                                className={styles.qr}
                                src={qr}
                                alt="Código QR para configurar Microsoft Authenticator"
                            />
                            <Input
                                className={styles.code}
                                value={codigo}
                                inputMode="numeric"
                                maxLength={6}
                                placeholder="Código de 6 dígitos"
                                aria-label="Código de Microsoft Authenticator"
                                onChange={(_event, valor) => setCodigo(valor.value.replace(/\D/g, '').slice(0, 6))}
                            />
                            <div className={styles.actions}>
                                <Button
                                    appearance="primary"
                                    disabled={verificandoAlta || codigo.length !== 6}
                                    onClick={confirmarAlta}
                                >
                                    {verificandoAlta ? 'Verificando…' : 'Verificar y terminar'}
                                </Button>
                                <Button
                                    appearance="secondary"
                                    disabled={verificandoAlta}
                                    onClick={() => {
                                        setQr(null)
                                        setCodigo('')
                                    }}
                                >
                                    Cancelar
                                </Button>
                            </div>
                        </div>
                    )}

                    <div className={styles.actions}>
                        <Button
                            appearance="primary"
                            disabled={
                                guardando ||
                                iniciandoAlta ||
                                verificandoAlta ||
                                !hayCambios ||
                                (configuracion.authenticatorEnabled === true &&
                                    configuracion.authenticatorConfigured !== true)
                            }
                            onClick={guardar}
                        >
                            {guardando ? 'Guardando…' : 'Guardar configuración'}
                        </Button>

                        <Button
                            appearance="secondary"
                            disabled={guardando || iniciandoAlta || verificandoAlta || !cambios}
                            onClick={descartar}
                        >
                            Descartar
                        </Button>
                    </div>
                </>
            )}
        </Card>
    )
}
