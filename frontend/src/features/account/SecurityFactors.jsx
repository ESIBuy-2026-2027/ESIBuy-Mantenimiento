
import { useCallback, useId, useState } from 'react'
import {
    Button,
    Card,
    Checkbox,
    MessageBar,
    MessageBarActions,
    MessageBarBody,
    Spinner,
    Text,
    Title3,
    makeStyles,
    tokens,
} from '@fluentui/react-components'
import { getSecurityFactors, updateSecurityFactors } from '../../api/security'
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
})

export default function SecurityFactors() {
    const styles = useStyles()
    const titleId = useId()
    const fetcher = useCallback(() => getSecurityFactors(), [])
    const { data, failed, retry } = useApiQuery(fetcher)

    const [cambios, setCambios] = useState(null)
    const [configuracionGuardada, setConfiguracionGuardada] = useState(null)
    const [guardando, setGuardando] = useState(false)
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
                                disabled={guardando}
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
                                disabled={guardando || configuracion?.authenticatorConfigured !== true}
                                onChange={(_event, option) => cambiar('authenticatorEnabled', option.checked === true)}
                            />
                            <Text className={styles.description}>
                                Utiliza códigos temporales generados por Microsoft Authenticator.
                            </Text>

                            {configuracion?.authenticatorConfigured !== true && (
                                <Text className={styles.description}>
                                    Para activar este método primero tienes que configurar y verificar Microsoft Authenticator.
                                </Text>
                            )}
                        </div>
                    </div>

                    <div className={styles.actions}>
                        <Button
                            appearance="primary"
                            disabled={
                                guardando ||
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
                            disabled={guardando || !cambios}
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
