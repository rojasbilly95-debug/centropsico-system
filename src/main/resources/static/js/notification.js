let stompClient = null;
let notificationData = [];

async function loadNotifications() {
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
        showPendingNotificationsAlert();

    } catch (error) {
        console.error("Error cargando notificaciones:", error);
    }
}

function mergeNotifications(roleNotifications, userNotifications) {
    const map = new Map();

    [...roleNotifications, ...userNotifications].forEach(notification => {
        map.set(notification.id, notification);
    });

    return Array.from(map.values())
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
}

function renderNotifications() {
    const list = document.getElementById("notificationList");
    if (!list) return;

    list.innerHTML = "";

    if (notificationData.length === 0) {
        list.innerHTML = `
            <div class="notification-empty">
                No hay notificaciones
            </div>
        `;
        return;
    }

    notificationData.slice(0, 10).forEach(notification => {
        list.innerHTML += `
            <div class="notification-item ${notification.read ? "" : "unread"}"
                 onclick="markNotificationAsRead(${notification.id})">
                <strong>${notification.title}</strong>
                <p>${notification.message}</p>
                <small>${formatNotificationDate(notification.createdAt)}</small>
            </div>
        `;
    });
}

function updateNotificationCount() {
    const count = notificationData.filter(notification => !notification.read).length;
    const badge = document.getElementById("notificationCount");

    if (!badge) return;

    if (count > 0) {
        badge.textContent = count;
        badge.classList.remove("hidden");
    } else {
        badge.textContent = "0";
        badge.classList.add("hidden");
    }
}

function toggleNotificationDropdown() {
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
    } else {
        closeNotificationDropdown();
    }
}

function closeNotificationDropdown() {
    const dropdown = document.getElementById("notificationDropdown");
    if (!dropdown) return;

    dropdown.classList.add("hidden");
    dropdown.style.display = "none";
}

async function markNotificationAsRead(id) {
    try {
        const response = await authFetch(`${baseUrl}/notifications/${id}/read`, {
            method: "PATCH"
        });

        if (!response || !response.ok) return;

        await loadNotifications();

    } catch (error) {
        console.error("Error marcando notificación:", error);
    }
}

async function markAllNotificationsAsRead() {
    try {
        if (!currentUser || !currentUser.role) return;

        await Promise.all([
            authFetch(`${baseUrl}/notifications/role/${currentUser.role}/read-all`, {
                method: "PATCH"
            }),
            authFetch(`${baseUrl}/notifications/me/read-all`, {
                method: "PATCH"
            })
        ]);

        await loadNotifications();

    } catch (error) {
        console.error("Error marcando todas las notificaciones:", error);
    }
}

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

function handleRealtimeNotification(notification) {
    const exists = notificationData.some(item => item.id === notification.id);

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
        text: notification.message,
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

function formatNotificationDate(value) {
    if (!value) return "";

    const date = new Date(value);

    return date.toLocaleString("es-PE", {
        dateStyle: "short",
        timeStyle: "short"
    });
}