async function openProfileModal() {
    try {
        const profile = await fetchMyProfile();

        if (!profile) {
            Swal.fire("Error", "No se pudo cargar tu perfil.", "error");
            return;
        }

        syncCurrentUser(profile);
        renderSidebarUser(currentUser);

        const initials = getUserInitials(profile);

        await Swal.fire({
            title: "Mi perfil",
            width: 760,
            html: `
                <div class="profile-modal">

                    <div class="profile-header-card">
                        <div class="profile-avatar-preview" id="profileAvatarPreview">
                            ${buildProfileAvatarHtml(
                                profile,
                                initials,
                                "Foto de perfil"
                            )}
                        </div>

                        <div class="profile-header-info">
                            <strong>
                                ${escapeProfileHtml(profile.firstName)}
                                ${escapeProfileHtml(profile.lastName)}
                            </strong>

                            <span>
                                ${escapeProfileHtml(profile.email)}
                            </span>

                            <small>
                                ${formatProfileRole(profile.role)}
                            </small>
                        </div>
                    </div>

                    <div class="profile-section">
                        <h4>Datos personales</h4>

                        <div class="profile-grid">
                            <input
                                id="profileFirstName"
                                type="text"
                                placeholder="Nombres"
                                value="${escapeProfileAttr(profile.firstName)}"
                            >

                            <input
                                id="profileLastName"
                                type="text"
                                placeholder="Apellidos"
                                value="${escapeProfileAttr(profile.lastName)}"
                            >

                            <input
                                id="profilePhone"
                                type="text"
                                placeholder="Teléfono"
                                value="${escapeProfileAttr(profile.phone)}"
                            >

                            <input
                                id="profileEmail"
                                type="email"
                                value="${escapeProfileAttr(profile.email)}"
                                disabled
                            >
                        </div>
                    </div>

                    <div class="profile-section">
                        <h4>Foto de perfil</h4>

                        <div class="profile-photo-actions">
                            <input
                                id="profilePhotoInput"
                                type="file"
                                accept="image/*"
                            >
                        </div>

                            <p class="profile-help">
                                Selecciona una fotografía. El sistema la optimizará y subirá automáticamente.
                            </p>
                    </div>

                    <div
                        id="profileModalResult"
                        class="profile-result"
                    ></div>

                </div>
            `,
            showCancelButton: true,
            showDenyButton: true,
            confirmButtonText: "Guardar datos",
            denyButtonText: "Cambiar contraseña",
            cancelButtonText: "Cerrar",
            focusConfirm: false,

            didOpen: () => {
                const photoInput =
                    document.getElementById("profilePhotoInput");

                if (photoInput) {
                    photoInput.addEventListener("change", async () => {
                        previewSelectedProfilePhoto();

                        showProfileModalMessage(
                            "Subiendo foto de perfil...",
                            true
                        );

                        await uploadProfilePhotoFromModal();
                    });
                }
            },

            preConfirm: async () => {
                const updated = await updateMyProfileFromModal();

                if (!updated) {
                    return false;
                }

                return updated;
            }
        }).then(async result => {
            if (result.isConfirmed && result.value) {
                await Swal.fire({
                    icon: "success",
                    title: "Datos personales actualizados",
                    text: "Tus nombres, apellidos y teléfono fueron guardados correctamente.",
                    timer: 1600,
                    showConfirmButton: false
                });
            }

            if (result.isDenied) {
                await openChangePasswordModal();
            }
        });

    } catch (error) {
        console.error("Error al abrir perfil:", error);

        Swal.fire(
            "Error",
            "No se pudo abrir el perfil.",
            "error"
        );
    }
}


/* =========================================================
   OBTENER PERFIL DEL USUARIO
========================================================= */

async function fetchMyProfile() {
    try {
        const response = await authFetch(
            `${baseUrl}/profile/me`
        );

        if (!response) {
            return null;
        }

        const result = await readProfileJsonResponse(response);

        if (!response.ok) {
            console.error(
                "No se pudo cargar el perfil:",
                result
            );

            return null;
        }

        return result;

    } catch (error) {
        console.error(
            "Error al consultar el perfil:",
            error
        );

        return null;
    }
}


/* =========================================================
   ACTUALIZAR DATOS PERSONALES
========================================================= */

async function updateMyProfileFromModal() {
    const firstNameInput =
        document.getElementById("profileFirstName");

    const lastNameInput =
        document.getElementById("profileLastName");

    const phoneInput =
        document.getElementById("profilePhone");

    const firstName =
        firstNameInput?.value.trim() || "";

    const lastName =
        lastNameInput?.value.trim() || "";

    const phone =
        phoneInput?.value.trim() || "";

    if (!firstName) {
        Swal.showValidationMessage(
            "Ingresa tus nombres."
        );

        return false;
    }

    if (!lastName) {
        Swal.showValidationMessage(
            "Ingresa tus apellidos."
        );

        return false;
    }

    try {
        const response = await authFetch(
            `${baseUrl}/profile/me`,
            {
                method: "PUT",
                body: JSON.stringify({
                    firstName,
                    lastName,
                    phone
                })
            }
        );

        if (!response) {
            return false;
        }

        const result =
            await readProfileJsonResponse(response);

    if (!response.ok) {
        Swal.showValidationMessage(
            result.detail ||
            result.message ||
            result.error ||
            "No se pudo actualizar el perfil."
        );

        return false;
    }

        syncCurrentUser(result);
        renderSidebarUser(currentUser);

        return result;

    } catch (error) {
        console.error(
            "Error al actualizar el perfil:",
            error
        );

        Swal.showValidationMessage(
            "Error de conexión al actualizar el perfil."
        );

        return false;
    }
}


/* =========================================================
   SUBIR FOTO DESDE EL MODAL
========================================================= */

async function uploadProfilePhotoFromModal() {
    const input =
        document.getElementById("profilePhotoInput");

    if (!input) {
        showProfileModalMessage(
            "No se encontró el campo para seleccionar la foto.",
            false
        );

        return null;
    }

    const result = await uploadProfilePhoto(input);

    if (!result) {
        return null;
    }

    updateProfileModalAvatar(result);

    showProfileModalMessage(
        "Foto actualizada correctamente.",
        true
    );

    input.value = "";

    return result;
}


/* =========================================================
   SUBIR FOTO AL ENDPOINT DEL BACKEND
========================================================= */

async function uploadProfilePhoto(input) {
    if (!input?.files || input.files.length === 0) {
        showProfileModalMessage(
            "Primero selecciona una fotografía.",
            false
        );

        return null;
    }

    const originalFile = input.files[0];

    if (!originalFile.type.startsWith("image/")) {
        showProfileModalMessage(
            "El archivo seleccionado no es una imagen válida.",
            false
        );

        input.value = "";
        return null;
    }

    try {
        showProfileModalMessage(
            "Procesando y optimizando la fotografía...",
            true
        );

        /*
         * La fotografía original puede pesar varios MB.
         * Antes de enviarla se reduce automáticamente.
         */
        const optimizedFile =
            await prepareProfileImage(originalFile);

        const formData = new FormData();

        formData.append(
            "photo",
            optimizedFile,
            optimizedFile.name
        );

        const token =
            localStorage.getItem("token");

        const headers = {};

        if (token) {
            headers.Authorization =
                `Bearer ${token}`;
        }

        /*
         * No colocar Content-Type manualmente.
         * El navegador genera multipart/form-data.
         */
        const response = await fetch(
            `${baseUrl}/profile/photo`,
            {
                method: "POST",
                headers,
                body: formData
            }
        );

        const result =
            await readProfileJsonResponse(response);

        if (response.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("currentUser");
            localStorage.removeItem("profileImageBase64");

            window.location.href = "/login.html";
            return null;
        }

        if (response.status === 403) {
            showProfileModalMessage(
                result.detail ||
                result.message ||
                result.error ||
                "No tienes permiso para actualizar la fotografía.",
                false
            );

            return null;
        }

        if (!response.ok) {
            showProfileModalMessage(
                result.detail ||
                result.message ||
                result.error ||
                `No se pudo subir la fotografía. Código: ${response.status}`,
                false
            );

            return null;
        }

        syncCurrentUser(result);
        renderSidebarUser(currentUser);

        return result;

    } catch (error) {
        console.error(
            "Error al procesar o subir la fotografía:",
            error
        );

        showProfileModalMessage(
            error.message ||
            "No se pudo procesar la fotografía seleccionada.",
            false
        );

        input.value = "";

        return null;
    }
}

async function prepareProfileImage(originalFile) {
    const objectUrl =
        URL.createObjectURL(originalFile);

    try {
        const image = await new Promise(
            (resolve, reject) => {
                const img = new Image();

                img.onload = () => resolve(img);

                img.onerror = () => {
                    reject(
                        new Error(
                            "El navegador no pudo leer esta fotografía."
                        )
                    );
                };

                img.src = objectUrl;
            }
        );

        /*
         * Para una foto de perfil no se necesita
         * conservar una resolución de cámara completa.
         */
        const maxSide = 1200;

        const originalWidth =
            image.naturalWidth || image.width;

        const originalHeight =
            image.naturalHeight || image.height;

        if (!originalWidth || !originalHeight) {
            throw new Error(
                "La fotografía seleccionada no tiene dimensiones válidas."
            );
        }

        const scale = Math.min(
            1,
            maxSide / Math.max(
                originalWidth,
                originalHeight
            )
        );

        const newWidth = Math.max(
            1,
            Math.round(originalWidth * scale)
        );

        const newHeight = Math.max(
            1,
            Math.round(originalHeight * scale)
        );

        const canvas =
            document.createElement("canvas");

        canvas.width = newWidth;
        canvas.height = newHeight;

        const context =
            canvas.getContext("2d");

        if (!context) {
            throw new Error(
                "No se pudo preparar la fotografía."
            );
        }

        /*
         * Fondo blanco para fotografías PNG
         * que contengan transparencia.
         */
        context.fillStyle = "#ffffff";

        context.fillRect(
            0,
            0,
            newWidth,
            newHeight
        );

        context.drawImage(
            image,
            0,
            0,
            newWidth,
            newHeight
        );

        let quality = 0.88;

        let blob = await canvasToProfileBlob(
            canvas,
            quality
        );

        /*
         * El backend permite 2 MB.
         * Reducimos la calidad automáticamente
         * hasta quedar por debajo de 1.8 MB.
         */
        const safeMaximumSize =
            1.8 * 1024 * 1024;

        while (
            blob.size > safeMaximumSize &&
            quality > 0.45
        ) {
            quality -= 0.08;

            blob = await canvasToProfileBlob(
                canvas,
                quality
            );
        }

        if (blob.size > safeMaximumSize) {
            throw new Error(
                "No se pudo optimizar suficientemente la fotografía."
            );
        }

        return new File(
            [blob],
            `profile-${Date.now()}.jpg`,
            {
                type: "image/jpeg",
                lastModified: Date.now()
            }
        );

    } finally {
        URL.revokeObjectURL(objectUrl);
    }
}

function canvasToProfileBlob(
    canvas,
    quality
) {
    return new Promise(
        (resolve, reject) => {
            canvas.toBlob(
                blob => {
                    if (!blob) {
                        reject(
                            new Error(
                                "No se pudo convertir la fotografía."
                            )
                        );

                        return;
                    }

                    resolve(blob);
                },
                "image/jpeg",
                quality
            );
        }
    );
}
/* =========================================================
   CAMBIAR CONTRASEÑA
========================================================= */

async function openChangePasswordModal() {
    await Swal.fire({
        title: "Cambiar contraseña",
        width: 520,

        html: `
            <div class="profile-password-modal">
                <input
                    id="profileCurrentPassword"
                    type="password"
                    placeholder="Contraseña actual"
                >

                <input
                    id="profileNewPassword"
                    type="password"
                    placeholder="Nueva contraseña"
                >

                <input
                    id="profileConfirmPassword"
                    type="password"
                    placeholder="Confirmar nueva contraseña"
                >

                <p>
                    La nueva contraseña debe tener
                    al menos 6 caracteres.
                </p>
            </div>
        `,

        showCancelButton: true,
        confirmButtonText: "Actualizar contraseña",
        cancelButtonText: "Cancelar",
        focusConfirm: false,

        preConfirm: async () => {
            const currentPassword =
                document.getElementById(
                    "profileCurrentPassword"
                )?.value || "";

            const newPassword =
                document.getElementById(
                    "profileNewPassword"
                )?.value || "";

            const confirmPassword =
                document.getElementById(
                    "profileConfirmPassword"
                )?.value || "";

            if (!currentPassword) {
                Swal.showValidationMessage(
                    "Ingresa tu contraseña actual."
                );

                return false;
            }

            if (
                !newPassword ||
                newPassword.length < 6
            ) {
                Swal.showValidationMessage(
                    "La nueva contraseña debe tener al menos 6 caracteres."
                );

                return false;
            }

            if (newPassword !== confirmPassword) {
                Swal.showValidationMessage(
                    "Las contraseñas no coinciden."
                );

                return false;
            }

            const response = await authFetch(
                `${baseUrl}/profile/change-password`,
                {
                    method: "PUT",
                    body: JSON.stringify({
                        currentPassword,
                        newPassword,
                        confirmPassword
                    })
                }
            );

            if (!response) {
                return false;
            }

            const result =
                await readProfileJsonResponse(response);

        if (!response.ok) {
            Swal.showValidationMessage(
                result.detail ||
                result.message ||
                result.error ||
                "No se pudo cambiar la contraseña."
            );

            return false;
        }

            return result;
        }

    }).then(result => {
        if (result.isConfirmed) {
            Swal.fire({
                icon: "success",
                title: "Contraseña actualizada",
                text: "Tu contraseña fue cambiada correctamente.",
                timer: 1700,
                showConfirmButton: false
            });
        }
    });
}


/* =========================================================
   VISTA PREVIA DE LA FOTO
========================================================= */

function previewSelectedProfilePhoto() {
    const input =
        document.getElementById("profilePhotoInput");

    const preview =
        document.getElementById(
            "profileAvatarPreview"
        );

    if (
        !input ||
        !preview ||
        !input.files ||
        input.files.length === 0
    ) {
        return;
    }

    const file = input.files[0];

    if (!file.type.startsWith("image/")) {
        return;
    }

    const temporaryUrl =
        URL.createObjectURL(file);

    preview.innerHTML = `
        <img
            src="${escapeProfileAttr(temporaryUrl)}"
            alt="Vista previa"
        >
    `;

    const previewImage =
        preview.querySelector("img");

    if (previewImage) {
        previewImage.onload = () => {
            URL.revokeObjectURL(temporaryUrl);
        };
    }
}


/* =========================================================
   ACTUALIZAR FOTO DEL MODAL
========================================================= */

function updateProfileModalAvatar(profile) {
    const preview =
        document.getElementById(
            "profileAvatarPreview"
        );

    if (!preview) {
        return;
    }

    preview.innerHTML =
        buildProfileAvatarHtml(
            profile,
            getUserInitials(profile),
            "Foto de perfil"
        );
}


/* =========================================================
   ACTUALIZAR USUARIO DEL SIDEBAR DESDE EL BACKEND
========================================================= */

async function refreshSidebarUser() {
    /*
     * Primero muestra la información disponible
     * mientras llega la respuesta del servidor.
     */
    if (
        typeof currentUser !== "undefined" &&
        currentUser
    ) {
        renderSidebarUser(currentUser);
    }

    /*
     * Después consulta MySQL mediante el backend.
     * Aquí se recupera profileImageBase64 después
     * de un redeploy de Render.
     */
    const profile = await fetchMyProfile();

    if (!profile) {
        return;
    }

    syncCurrentUser(profile);
    renderSidebarUser(currentUser);
}


/* =========================================================
   MOSTRAR USUARIO EN EL SIDEBAR
========================================================= */

function renderSidebarUser(user) {
    if (!user) {
        return;
    }

    const avatar =
        document.getElementById(
            "sidebarUserAvatar"
        );

    const name =
        document.getElementById(
            "sidebarUserName"
        );

    const role =
        document.getElementById(
            "sidebarUserRole"
        );

    if (name) {
        const fullName = `
            ${user.firstName || ""}
            ${user.lastName || ""}
        `.replace(/\s+/g, " ").trim();

        name.textContent =
            fullName || "Usuario";
    }

    if (role) {
        role.textContent =
            formatProfileRole(user.role);
    }

    if (avatar) {
        avatar.innerHTML =
            buildProfileAvatarHtml(
                user,
                getUserInitials(user),
                "Foto de perfil",
                "sidebar-profile-img"
            );
    }
}


/* =========================================================
   SINCRONIZAR CURRENT USER
========================================================= */

function syncCurrentUser(profile) {
    if (!profile) {
        return;
    }

    const previousUser =
        typeof currentUser !== "undefined" &&
        currentUser
            ? currentUser
            : {};

    currentUser = {
        ...previousUser,

        id:
            profile.id ??
            previousUser.id,

        firstName:
            profile.firstName ??
            previousUser.firstName ??
            "",

        lastName:
            profile.lastName ??
            previousUser.lastName ??
            "",

        email:
            profile.email ??
            previousUser.email ??
            "",

        role:
            profile.role ??
            previousUser.role ??
            "",

        phone:
            profile.phone ??
            previousUser.phone ??
            "",

        /*
         * Primera opción:
         * imagen persistente guardada en MySQL.
         */
        profileImageBase64:
            profile.profileImageBase64 ??
            previousUser.profileImageBase64 ??
            "",

        /*
         * Segunda opción:
         * URL antigua como respaldo.
         */
        profileImageUrl:
            profile.profileImageUrl ??
            previousUser.profileImageUrl ??
            ""
    };

    try {
        /*
         * No guardamos la imagen Base64 en localStorage,
         * porque una imagen grande puede superar su límite.
         *
         * La imagen siempre se volverá a consultar
         * desde el backend y MySQL.
         */
        const userForStorage = {
            ...currentUser,
            profileImageBase64: ""
        };

        localStorage.setItem(
            "currentUser",
            JSON.stringify(userForStorage)
        );

        /*
         * Elimina datos antiguos que pudieron quedar
         * guardados por la versión anterior.
         */
        localStorage.removeItem(
            "profileImageBase64"
        );

    } catch (error) {
        console.warn(
            "No se pudo actualizar currentUser en localStorage:",
            error
        );
    }
}


/* =========================================================
   CONSTRUIR HTML DEL AVATAR
========================================================= */

function buildProfileAvatarHtml(
    user,
    initials,
    altText,
    imageClass = ""
) {
    const imageSource =
        getProfileImageSource(user);

    if (!imageSource) {
        return `
            <span>
                ${escapeProfileHtml(initials)}
            </span>
        `;
    }

    const finalSource =
        addProfileImageCacheBuster(imageSource);

    const classAttribute =
        imageClass
            ? ` class="${escapeProfileAttr(imageClass)}"`
            : "";

    return `
        <img
            src="${escapeProfileAttr(finalSource)}"
            alt="${escapeProfileAttr(altText)}"
            ${classAttribute}
        >
    `;
}


/* =========================================================
   OBTENER FUENTE DE LA IMAGEN
========================================================= */

function getProfileImageSource(user) {
    /*
     * Prioridad 1:
     * Base64 almacenado en MySQL.
     */
    const base64Image = String(
        user?.profileImageBase64 || ""
    ).trim();

    if (
        /^data:image\/(jpeg|jpg|png|webp);base64,/i
            .test(base64Image)
    ) {
        return base64Image;
    }

    /*
     * Prioridad 2:
     * URL antigua como respaldo.
     */
    return getProfileImageUrl(
        user?.profileImageUrl
    );
}


/* =========================================================
   EVITAR CACHÉ SOLO PARA URL
========================================================= */

function addProfileImageCacheBuster(source) {
    /*
     * Nunca agregar ?t= a una imagen Base64.
     * Eso dañaría el Data URI.
     */
    if (
        !source ||
        source.startsWith("data:")
    ) {
        return source || "";
    }

    const separator =
        source.includes("?")
            ? "&"
            : "?";

    return `${source}${separator}t=${Date.now()}`;
}


/* =========================================================
   MENSAJE EN EL MODAL
========================================================= */

function showProfileModalMessage(
    message,
    success
) {
    const resultBox =
        document.getElementById(
            "profileModalResult"
        );

    if (!resultBox) {
        return;
    }

    resultBox.className =
        success
            ? "profile-result success"
            : "profile-result error";

    resultBox.textContent = message;
}


/* =========================================================
   NORMALIZAR URL DE FOTO ANTIGUA
========================================================= */

function getProfileImageUrl(url) {
    const normalizedUrl =
        String(url || "").trim();

    if (!normalizedUrl) {
        return "";
    }

    if (
        normalizedUrl.startsWith("http://") ||
        normalizedUrl.startsWith("https://") ||
        normalizedUrl.startsWith("/")
    ) {
        return normalizedUrl;
    }

    return normalizedUrl;
}


/* =========================================================
   OBTENER INICIALES
========================================================= */

function getUserInitials(user) {
    const first =
        user?.firstName
            ? user.firstName.charAt(0)
            : "";

    const last =
        user?.lastName
            ? user.lastName.charAt(0)
            : "";

    const initials =
        `${first}${last}`.toUpperCase();

    return initials || "US";
}


/* =========================================================
   FORMATEAR ROL
========================================================= */

function formatProfileRole(role) {
    switch (role) {
        case "ADMIN":
            return "ADMIN";

        case "RECEPCIONISTA":
            return "RECEPCIÓN";

        case "PSICOLOGO":
            return "PSICÓLOGO";

        default:
            return role || "USUARIO";
    }
}


/* =========================================================
   ESCAPAR HTML
========================================================= */

function escapeProfileHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeProfileAttr(value) {
    return escapeProfileHtml(value);
}


/* =========================================================
   LEER RESPUESTAS DEL BACKEND
========================================================= */

async function readProfileJsonResponse(response) {
    const responseText =
        await response.text();

    if (!responseText) {
        return {};
    }

    try {
        return JSON.parse(responseText);

    } catch (error) {
        console.error(
            "La respuesta del servidor no es JSON:",
            responseText
        );

        return {
            message: responseText
        };
    }
}


/* =========================================================
   ABRIR PERFIL DESDE EL SIDEBAR
========================================================= */

function initProfileSidebarClick() {
    const sidebarUser =
        document.getElementById(
            "sidebarUserProfile"
        ) ||
        document.querySelector(
            ".sidebar-user"
        );

    if (!sidebarUser) {
        console.warn(
            "No se encontró el bloque del usuario en el sidebar."
        );

        return;
    }

    sidebarUser.onclick =
        async function (event) {
            event.preventDefault();
            event.stopPropagation();

            await openProfileModal();
        };

    sidebarUser.style.cursor = "pointer";

    sidebarUser.setAttribute(
        "title",
        "Abrir mi perfil"
    );
}