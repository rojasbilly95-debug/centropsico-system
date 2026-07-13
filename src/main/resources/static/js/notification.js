let stompClient = null;
let notificationData = [];
let notificationsDropdownOpen = false;
let markingNotificationsAsRead = false;
let expandedNotificationIds = new Set();

/* =========================
   CARGA DE NOTIFICACIONES
========================= */

async function loadNotifications(showAlert = true) {
    try {
        if (!currentUser || !currentUser.role) return;

        const roleUrl = `${baseUrl}/notifications/role/${currentUser.role}`;
        const userUrl = `${baseUrl}/notifications/me`;

        const [roleResponse, userResponse] = await Promise.all([
            authFetch(roleUrl),
            authFetch(userUrl)
        ]);

        let roleNotifications = [];
        let userNotifications = [];

        if (roleResponse && roleResponse.ok) {
            roleNotifications = await roleResponse.json();
        }

        if (userResponse && userResponse.ok) {
            userNotifications = await userResponse.json();
        }

        notificationData = mergeNotifications(roleNotifications, userNotifications);

        renderNotifications();
        updateNotificationCount();

        if (showAlert) {
            showPendingNotificationsAlert();
        }

    } catch (error) {
        console.error("Error cargando notificaciones:", error);

        const list = document.getElementById("notificationList");

        if (list) {
            list.innerHTML = `
                <div class="notification-empty">
                    No se pudieron cargar las notificaciones.
                </div>
            `;
        }
    }
}

function mergeNotifications(roleNotifications = [], userNotifications = []) {
    const map = new Map();

    [...roleNotifications, ...userNotifications].forEach(notification => {
        if (notification && notification.id != null) {
            map.set(Number(notification.id), notification);
        }
    });

    return Array.from(map.values())
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
}

/* =========================
   RENDER
========================= */

function renderNotifications() {
    const list = document.getElementById("notificationList");

    if (!list) return;

    list.innerHTML = "";

    if (!notificationData || notificationData.length === 0) {
        list.innerHTML = `
            <div class="notification-empty">
                No hay notificaciones
            </div>
        `;
        return;
    }

    notificationData.slice(0, 15).forEach(notification => {
        const notificationId = Number(notification.id);
        const isUnread = !notification.read;
        const preview = getNotificationPreview(notification.message || "", 14);

        list.innerHTML += `
            <div class="notification-item ${isUnread ? "unread" : ""}"
                 onclick="openNotificationDetail(${notificationId})">

                <div class="notification-item-header">
                    <strong>${escapeNotificationHtml(notification.title || "Notificación")}</strong>

                    ${
                        isUnread
                            ? `<span class="notification-new-badge">Nuevo</span>`
                            : ""
                    }
                </div>

                <p class="notification-preview">
                    ${escapeNotificationHtml(preview)}
                </p>

                <div class="notification-item-footer">
                    <small>${formatNotificationDate(notification.createdAt)}</small>

                    <button 
                        type="button"
                        class="notification-open-btn"
                        onclick="event.stopPropagation(); openNotificationDetail(${notificationId})">
                        Abrir
                    </button>
                </div>
            </div>
        `;
    });
}

async function openNotificationDetail(id) {
    const notificationId = Number(id);

    if (!notificationId) return;

    const notification = notificationData.find(item => Number(item.id) === notificationId);

    if (!notification) return;

    // Cerrar/minimizar el panel de notificaciones antes de mostrar el mensaje
    closeNotificationDropdown();

    if (!notification.read) {
        await markNotificationAsRead(notificationId);
    }

    await Swal.fire({
        title: escapeNotificationHtml(notification.title || "Notificación"),
        html: `
            <div class="notification-detail-modal">

                <div class="notification-detail-meta">
                    <span>Mensaje recibido</span>
                    <small>${formatNotificationDate(notification.createdAt)}</small>
                </div>

                <div class="notification-detail-message">
                    ${escapeNotificationHtml(notification.message || "Sin detalle")}
                </div>

            </div>
        `,
        confirmButtonText: "Cerrar mensaje",
        confirmButtonColor: "#0f3d66",
        width: 560,
        backdrop: true,
        allowOutsideClick: true,
        heightAuto: false,
        customClass: {
            popup: "notification-detail-popup"
        }
    });

    renderNotifications();
    updateNotificationCount();
}

async function toggleNotificationItem(id) {
    const notificationId = Number(id);

    if (!notificationId) return;

    const notification = notificationData.find(item => Number(item.id) === notificationId);

    if (!notification) return;

    if (expandedNotificationIds.has(notificationId)) {
        expandedNotificationIds.delete(notificationId);
    } else {
        expandedNotificationIds.add(notificationId);

        if (!notification.read) {
            await markNotificationAsRead(notificationId);
            return;
        }
    }

    renderNotifications();
    updateNotificationCount();
}

function getNotificationPreview(message, maxWords = 14) {
    const text = String(message || "").trim();

    if (!text) return "Sin detalle";

    const words = text.split(/\s+/);

    if (words.length <= maxWords) {
        return text.length > 90 ? text.substring(0, 90) + "..." : text;
    }

    return words.slice(0, maxWords).join(" ") + "...";
}

function updateNotificationCount() {
    const badge = document.getElementById("notificationCount");

    if (!badge) return;

    const count = notificationData.filter(notification => !notification.read).length;

    if (count > 0) {
        badge.textContent = count;
        badge.classList.remove("hidden");
    } else {
        badge.textContent = "0";
        badge.classList.add("hidden");
    }
}

/* =========================
   DROPDOWN
========================= */

async function toggleNotificationDropdown() {
    const dropdown = document.getElementById("notificationDropdown");

    if (!dropdown) {
        console.error("No existe #notificationDropdown");
        return;
    }

    if (dropdown.parentElement !== document.body) {
        document.body.appendChild(dropdown);
    }

    const isHidden = dropdown.classList.contains("hidden");

    if (isHidden) {
        dropdown.classList.remove("hidden");
        dropdown.style.display = "block";
        notificationsDropdownOpen = true;

        await loadNotifications(false);
        renderNotifications();

    } else {
        closeNotificationDropdown();
    }
}

function closeNotificationDropdown() {
    const dropdown = document.getElementById("notificationDropdown");

    if (!dropdown) return;

    dropdown.classList.add("hidden");
    dropdown.style.display = "none";
    notificationsDropdownOpen = false;
}

/* =========================
   MARCAR COMO LEÍDO
========================= */

async function markNotificationAsRead(id) {
    try {
        const notificationId = Number(id);

        const notification = notificationData.find(item => Number(item.id) === notificationId);

        if (notification && notification.read) {
            renderNotifications();
            updateNotificationCount();
            return;
        }

        notificationData = notificationData.map(item => {
            if (Number(item.id) === notificationId) {
                return {
                    ...item,
                    read: true
                };
            }

            return item;
        });

        renderNotifications();
        updateNotificationCount();

        const response = await authFetch(`${baseUrl}/notifications/${notificationId}/read`, {
            method: "PATCH"
        });

        if (!response || !response.ok) {
            await loadNotifications(false);
        }

    } catch (error) {
        console.error("Error marcando notificación:", error);
        await loadNotifications(false);
    }
}

async function markAllNotificationsAsRead() {
    try {
        if (!currentUser || !currentUser.role) return;

        notificationData = notificationData.map(notification => ({
            ...notification,
            read: true
        }));

        renderNotifications();
        updateNotificationCount();

        await Promise.all([
            authFetch(`${baseUrl}/notifications/role/${currentUser.role}/read-all`, {
                method: "PATCH"
            }),
            authFetch(`${baseUrl}/notifications/me/read-all`, {
                method: "PATCH"
            })
        ]);

        await loadNotifications(false);

    } catch (error) {
        console.error("Error marcando todas las notificaciones:", error);
        await loadNotifications(false);
    }
}

async function markVisibleNotificationsAsRead() {
    try {
        if (markingNotificationsAsRead) return;
        if (!currentUser || !currentUser.role) return;

        const hasUnread = notificationData.some(notification => !notification.read);

        if (!hasUnread) {
            updateNotificationCount();
            return;
        }

        markingNotificationsAsRead = true;

        notificationData = notificationData.map(notification => ({
            ...notification,
            read: true
        }));

        renderNotifications();
        updateNotificationCount();

        await Promise.all([
            authFetch(`${baseUrl}/notifications/role/${currentUser.role}/read-all`, {
                method: "PATCH"
            }),
            authFetch(`${baseUrl}/notifications/me/read-all`, {
                method: "PATCH"
            })
        ]);

        await loadNotifications(false);

    } catch (error) {
        console.error("Error marcando notificaciones visibles como leídas:", error);
        await loadNotifications(false);

    } finally {
        markingNotificationsAsRead = false;
    }
}

/* =========================
   WEBSOCKET
========================= */

function connectNotificationWebSocket() {
    if (!currentUser || !currentUser.role) return;

    if (stompClient && stompClient.connected) {
        return;
    }

    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.debug = null;

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/notifications/role/${currentUser.role}`, message => {
            handleRealtimeNotification(JSON.parse(message.body));
        });

        if (currentUser.email) {
            stompClient.subscribe(`/topic/notifications/user/${currentUser.email}`, message => {
                handleRealtimeNotification(JSON.parse(message.body));
            });
        }
    }, error => {
        console.error("Error WebSocket:", error);

        setTimeout(() => {
            connectNotificationWebSocket();
        }, 5000);
    });
}

async function handleRealtimeNotification(notification) {
    const exists = notificationData.some(item => Number(item.id) === Number(notification.id));

    if (!exists) {
        notificationData.unshift(notification);
    }

    renderNotifications();
    updateNotificationCount();
    animateNotificationBell();
    showRealtimeToast(notification);
    refreshRealtimeModules();
}

function showRealtimeToast(notification) {
    if (typeof Swal === "undefined") return;

    Swal.fire({
        toast: true,
        position: "top-end",
        icon: "info",
        title: notification.title,
        text: getNotificationPreview(notification.message, 14),
        timer: 3500,
        showConfirmButton: false
    });
}

function refreshRealtimeModules() {
    if (typeof refreshAppointmentsRealtime === "function") {
        refreshAppointmentsRealtime();
    }

    if (typeof refreshLeadsRealtime === "function") {
        refreshLeadsRealtime();
    }

    if (typeof refreshDashboardRealtime === "function") {
        refreshDashboardRealtime();
    }

    if (typeof refreshFinancesRealtime === "function") {
        refreshFinancesRealtime();
    }
}

function animateNotificationBell() {
    const button = document.querySelector(".notification-btn");

    if (!button) {
        console.warn("No se encontró .notification-btn");
        return;
    }

    button.classList.remove("has-new");
    void button.offsetWidth;
    button.classList.add("has-new");

    setTimeout(() => {
        button.classList.remove("has-new");
    }, 3200);
}

/* =========================
   ALERTA INICIAL
========================= */

function showPendingNotificationsAlert() {
    const pending = notificationData.filter(notification => !notification.read);

    if (pending.length === 0) return;

    if (sessionStorage.getItem("pendingNotificationsAlertShown") === "true") return;

    sessionStorage.setItem("pendingNotificationsAlertShown", "true");

    if (typeof Swal === "undefined") return;

    Swal.fire({
        icon: "info",
        title: "Tienes notificaciones pendientes",
        html: `
            <p>Hay <strong>${pending.length}</strong> notificación(es) sin revisar.</p>
            <p>Revísalas para no pasar por alto citas, pagos o registros importantes.</p>
        `,
        confirmButtonText: "Ver notificaciones",
        showCancelButton: true,
        cancelButtonText: "Cerrar",
        confirmButtonColor: "#0f3d66"
    }).then(result => {
        if (result.isConfirmed) {
            toggleNotificationDropdown();
        }
    });
}

/* =========================
   HELPERS
========================= */

function formatNotificationDate(value) {
    if (!value) return "";

    const date = new Date(value);

    return date.toLocaleString("es-PE", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

function escapeNotificationHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}