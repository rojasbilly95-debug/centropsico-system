const RESET_TOKEN_STORAGE_KEY =
    "centropsico.passwordResetToken";

const resetLoadingPanel =
    document.getElementById(
        "resetLoadingPanel"
    );

const resetInvalidPanel =
    document.getElementById(
        "resetInvalidPanel"
    );

const resetFormPanel =
    document.getElementById(
        "resetFormPanel"
    );

const resetSuccessPanel =
    document.getElementById(
        "resetSuccessPanel"
    );

const resetInvalidMessage =
    document.getElementById(
        "resetInvalidMessage"
    );

const resetPasswordForm =
    document.getElementById(
        "resetPasswordForm"
    );

const newPasswordInput =
    document.getElementById(
        "newPassword"
    );

const confirmPasswordInput =
    document.getElementById(
        "confirmPassword"
    );

const resetMessage =
    document.getElementById(
        "resetMessage"
    );

const passwordMatchMessage =
    document.getElementById(
        "passwordMatchMessage"
    );

const resetSubmitButton =
    document.getElementById(
        "resetSubmitButton"
    );

const passwordStrengthBar =
    document.getElementById(
        "passwordStrengthBar"
    );

const passwordStrengthText =
    document.getElementById(
        "passwordStrengthText"
    );

let resetToken = "";

document.addEventListener(
    "DOMContentLoaded",
    initializeResetPage
);

async function initializeResetPage() {
    const url =
        new URL(window.location.href);

    const tokenFromUrl =
        url.searchParams.get("token");

    if (tokenFromUrl) {
        sessionStorage.setItem(
            RESET_TOKEN_STORAGE_KEY,
            tokenFromUrl
        );

        /*
         * Retira el token de la barra de direcciones
         * y del historial visible del navegador.
         */
        window.history.replaceState(
            {},
            document.title,
            "/reset-password.html"
        );
    }

    resetToken =
        sessionStorage.getItem(
            RESET_TOKEN_STORAGE_KEY
        ) || "";

    initializePasswordEvents();

    if (!resetToken) {
        showInvalidResetLink(
            "El enlace de recuperación no contiene "
                + "un token válido."
        );

        return;
    }

    await validateResetToken();
}

async function validateResetToken() {
    try {
        const response =
            await fetch(
                `/api/auth/reset-password/validate?token=${
                    encodeURIComponent(resetToken)
                }`,
                {
                    method: "GET",
                    cache: "no-store"
                }
            );

        const result =
            await readJsonSafely(response);

        if (
            !response.ok
            || result.valid !== true
        ) {
            sessionStorage.removeItem(
                RESET_TOKEN_STORAGE_KEY
            );

            showInvalidResetLink(
                "El enlace es inválido, ya fue utilizado "
                    + "o ha expirado."
            );

            return;
        }

        resetLoadingPanel.hidden = true;
        resetFormPanel.hidden = false;

        newPasswordInput?.focus();

    } catch (error) {
        console.error(
            "Error validando el token:",
            error
        );

        showInvalidResetLink(
            "No se pudo verificar el enlace. "
                + "Comprueba tu conexión e inténtalo nuevamente."
        );
    }
}

function initializePasswordEvents() {
    newPasswordInput?.addEventListener(
        "input",
        () => {
            evaluatePassword();
            evaluatePasswordMatch();
        }
    );

    confirmPasswordInput?.addEventListener(
        "input",
        evaluatePasswordMatch
    );

    document
        .querySelectorAll(
            ".password-toggle"
        )
        .forEach(button => {
            button.addEventListener(
                "click",
                () => togglePasswordVisibility(
                    button
                )
            );
        });

    resetPasswordForm?.addEventListener(
        "submit",
        submitNewPassword
    );
}

async function submitNewPassword(
    event
) {
    event.preventDefault();

    const newPassword =
        newPasswordInput?.value || "";

    const confirmPassword =
        confirmPasswordInput?.value || "";

    const validation =
        getPasswordValidation(
            newPassword
        );

    if (!Object.values(validation).every(Boolean)) {
        showResetMessage(
            "La contraseña todavía no cumple "
                + "todos los requisitos.",
            "error"
        );

        newPasswordInput?.focus();
        return;
    }

    if (newPassword !== confirmPassword) {
        showResetMessage(
            "Las contraseñas no coinciden.",
            "error"
        );

        confirmPasswordInput?.focus();
        return;
    }

    setResetLoading(true);

    showResetMessage(
        "",
        ""
    );

    try {
        const response =
            await fetch(
                "/api/auth/reset-password",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        token: resetToken,
                        newPassword,
                        confirmPassword
                    })
                }
            );

        const result =
            await readJsonSafely(response);

        if (!response.ok) {
            showResetMessage(
                result.message
                    || "No se pudo actualizar la contraseña.",
                "error"
            );

            if (
                response.status === 400
                && String(result.message || "")
                    .toLowerCase()
                    .includes("enlace")
            ) {
                sessionStorage.removeItem(
                    RESET_TOKEN_STORAGE_KEY
                );
            }

            return;
        }

        sessionStorage.removeItem(
            RESET_TOKEN_STORAGE_KEY
        );

        resetToken = "";

        resetFormPanel.hidden = true;
        resetSuccessPanel.hidden = false;

        startLoginRedirect(5);

    } catch (error) {
        console.error(
            "Error actualizando contraseña:",
            error
        );

        showResetMessage(
            "No se pudo conectar con el servidor. "
                + "Inténtalo nuevamente.",
            "error"
        );

    } finally {
        setResetLoading(false);
    }
}

function evaluatePassword() {
    const password =
        newPasswordInput?.value || "";

    const validation =
        getPasswordValidation(password);

    Object.entries(validation)
        .forEach(([rule, valid]) => {
            const element =
                document.querySelector(
                    `[data-rule="${rule}"]`
                );

            if (!element) {
                return;
            }

            element.classList.toggle(
                "valid",
                valid
            );
        });

    const score =
        Object.values(validation)
            .filter(Boolean)
            .length;

    const percentage =
        (score / 5) * 100;

    if (passwordStrengthBar) {
        passwordStrengthBar.style.width =
            `${percentage}%`;

        passwordStrengthBar.dataset.score =
            String(score);
    }

    if (passwordStrengthText) {
        passwordStrengthText.textContent =
            getStrengthLabel(
                score,
                password.length
            );
    }
}

function evaluatePasswordMatch() {
    const password =
        newPasswordInput?.value || "";

    const confirmation =
        confirmPasswordInput?.value || "";

    if (!confirmation) {
        passwordMatchMessage.textContent = "";
        passwordMatchMessage.className =
            "password-match-message";

        return;
    }

    if (password === confirmation) {
        passwordMatchMessage.textContent =
            "Las contraseñas coinciden.";

        passwordMatchMessage.className =
            "password-match-message valid";

    } else {
        passwordMatchMessage.textContent =
            "Las contraseñas no coinciden.";

        passwordMatchMessage.className =
            "password-match-message invalid";
    }
}

function getPasswordValidation(
    password
) {
    return {
        length:
            password.length >= 10
            && password.length <= 72,

        uppercase:
            /[A-ZÁÉÍÓÚÑ]/.test(password),

        lowercase:
            /[a-záéíóúñ]/.test(password),

        number:
            /\d/.test(password),

        special:
            /[^\p{L}\p{N}\s]/u.test(password)
    };
}

function getStrengthLabel(
    score,
    length
) {
    if (!length) {
        return "Seguridad de la contraseña";
    }

    if (score <= 2) {
        return "Contraseña débil";
    }

    if (score <= 4) {
        return "Contraseña aceptable";
    }

    return "Contraseña segura";
}

function togglePasswordVisibility(
    button
) {
    const targetId =
        button.dataset.target;

    const input =
        document.getElementById(
            targetId
        );

    if (!input) {
        return;
    }

    const showPassword =
        input.type === "password";

    input.type =
        showPassword
            ? "text"
            : "password";

    button.textContent =
        showPassword
            ? "Ocultar"
            : "Ver";

    button.setAttribute(
        "aria-label",
        showPassword
            ? "Ocultar contraseña"
            : "Mostrar contraseña"
    );
}

function showInvalidResetLink(
    message
) {
    resetLoadingPanel.hidden = true;
    resetFormPanel.hidden = true;
    resetSuccessPanel.hidden = true;
    resetInvalidPanel.hidden = false;

    if (resetInvalidMessage) {
        resetInvalidMessage.textContent =
            message;
    }
}

function showResetMessage(
    message,
    type
) {
    if (!resetMessage) {
        return;
    }

    resetMessage.textContent =
        message || "";

    resetMessage.className =
        "recovery-message";

    if (type) {
        resetMessage.classList.add(
            `recovery-message-${type}`
        );
    }
}

function setResetLoading(
    loading
) {
    if (!resetSubmitButton) {
        return;
    }

    const text =
        resetSubmitButton.querySelector(
            ".button-text"
        );

    const loader =
        resetSubmitButton.querySelector(
            ".button-loader"
        );

    resetSubmitButton.disabled =
        loading;

    if (text) {
        text.hidden = loading;
    }

    if (loader) {
        loader.hidden = !loading;
    }
}

function startLoginRedirect(
    seconds
) {
    const redirectMessage =
        document.getElementById(
            "redirectMessage"
        );

    let remaining =
        seconds;

    const updateMessage = () => {
        if (redirectMessage) {
            redirectMessage.textContent =
                `Serás redirigido al inicio de sesión `
                + `en ${remaining} segundos.`;
        }
    };

    updateMessage();

    const interval =
        setInterval(() => {
            remaining--;

            if (remaining <= 0) {
                clearInterval(interval);

                window.location.replace(
                    "/login.html"
                );

                return;
            }

            updateMessage();

        }, 1000);
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