const forgotForm =
    document.getElementById(
        "forgotPasswordForm"
    );

const forgotFormPanel =
    document.getElementById(
        "forgotFormPanel"
    );

const forgotSuccessPanel =
    document.getElementById(
        "forgotSuccessPanel"
    );

const recoveryEmailInput =
    document.getElementById(
        "recoveryEmail"
    );

const forgotMessage =
    document.getElementById(
        "forgotMessage"
    );

const forgotSubmitButton =
    document.getElementById(
        "forgotSubmitButton"
    );

const resendRecoveryButton =
    document.getElementById(
        "resendRecoveryButton"
    );

const maskedRecoveryEmail =
    document.getElementById(
        "maskedRecoveryEmail"
    );

let lastRequestedEmail = "";
let resendInterval = null;

forgotForm?.addEventListener(
    "submit",
    async event => {
        event.preventDefault();

        const email =
            recoveryEmailInput
                ?.value
                ?.trim()
                ?.toLowerCase();

        if (!isValidEmail(email)) {
            showForgotMessage(
                "Ingresa un correo electrónico válido.",
                "error"
            );

            recoveryEmailInput?.focus();
            return;
        }

        await sendRecoveryRequest(
            email,
            false
        );
    }
);

resendRecoveryButton?.addEventListener(
    "click",
    async () => {
        if (
            !lastRequestedEmail
            || resendRecoveryButton.disabled
        ) {
            return;
        }

        resendRecoveryButton.disabled = true;
        resendRecoveryButton.textContent =
            "Enviando nuevamente...";

        await sendRecoveryRequest(
            lastRequestedEmail,
            true
        );
    }
);

async function sendRecoveryRequest(
    email,
    isResend
) {
    setForgotLoading(
        !isResend
    );

    showForgotMessage(
        "",
        ""
    );

    try {
        const response =
            await fetch(
                "/api/auth/forgot-password",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        email
                    })
                }
            );

        const result =
            await readJsonSafely(response);

        if (!response.ok) {
            showForgotMessage(
                result.message
                    || "No se pudo procesar la solicitud.",
                "error"
            );

            if (isResend) {
                resendRecoveryButton.disabled =
                    false;

                resendRecoveryButton.textContent =
                    "Reenviar enlace";
            }

            return;
        }

        lastRequestedEmail = email;

        maskedRecoveryEmail.textContent =
            maskEmail(email);

        forgotFormPanel.hidden = true;
        forgotSuccessPanel.hidden = false;

        startResendCountdown(60);

    } catch (error) {
        console.error(
            "Error solicitando recuperación:",
            error
        );

        showForgotMessage(
            "No se pudo conectar con el servidor. "
                + "Inténtalo nuevamente.",
            "error"
        );

        if (isResend) {
            resendRecoveryButton.disabled =
                false;

            resendRecoveryButton.textContent =
                "Reenviar enlace";
        }

    } finally {
        setForgotLoading(false);
    }
}

function startResendCountdown(
    seconds
) {
    if (resendInterval) {
        clearInterval(resendInterval);
    }

    let remaining = seconds;

    resendRecoveryButton.disabled = true;

    resendRecoveryButton.textContent =
        `Reenviar en ${remaining} segundos`;

    resendInterval = setInterval(() => {
        remaining--;

        if (remaining <= 0) {
            clearInterval(resendInterval);

            resendRecoveryButton.disabled =
                false;

            resendRecoveryButton.textContent =
                "Reenviar enlace";

            return;
        }

        resendRecoveryButton.textContent =
            `Reenviar en ${remaining} segundos`;

    }, 1000);
}

function setForgotLoading(
    loading
) {
    if (!forgotSubmitButton) {
        return;
    }

    const text =
        forgotSubmitButton.querySelector(
            ".button-text"
        );

    const loader =
        forgotSubmitButton.querySelector(
            ".button-loader"
        );

    forgotSubmitButton.disabled =
        loading;

    if (text) {
        text.hidden = loading;
    }

    if (loader) {
        loader.hidden = !loading;
    }
}

function showForgotMessage(
    message,
    type
) {
    if (!forgotMessage) {
        return;
    }

    forgotMessage.textContent =
        message || "";

    forgotMessage.className =
        "recovery-message";

    if (type) {
        forgotMessage.classList.add(
            `recovery-message-${type}`
        );
    }
}

function isValidEmail(
    email
) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        .test(email || "");
}

function maskEmail(
    email
) {
    const parts =
        String(email).split("@");

    if (parts.length !== 2) {
        return email;
    }

    const local =
        parts[0];

    const domain =
        parts[1];

    const firstCharacter =
        local.charAt(0);

    const hiddenPart =
        "*".repeat(
            Math.max(
                3,
                local.length - 1
            )
        );

    return `${firstCharacter}${hiddenPart}@${domain}`;
}

async function readJsonSafely(
    response
) {
    try {
        return await response.json();

    } catch (error) {
        return {};
    }
}