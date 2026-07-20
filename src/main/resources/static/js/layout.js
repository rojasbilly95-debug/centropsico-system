async function loadSidebar() {
    const container =
        document.getElementById("sidebar-container");

    if (!container) {
        throw new Error(
            "No se encontró #sidebar-container."
        );
    }

    const response = await fetch(
        "/components/sidebar.html",
        {
            cache: "no-store"
        }
    );

    if (!response.ok) {
        throw new Error(
            `No se pudo cargar el sidebar. Código: ${response.status}`
        );
    }

    container.innerHTML = await response.text();

    loadSidebarUser();
    applyRoleTheme();
    applyRoleVisibility();
    applySectionSecurity();

    if (window.lucide) {
        lucide.createIcons();
    }
}

function loadSidebarUser() {
    if (!currentUser) return;

    const fullName = `${currentUser.firstName} ${currentUser.lastName}`;
    const initials = `${currentUser.firstName?.charAt(0) ?? ""}${currentUser.lastName?.charAt(0) ?? ""}`;

    document.getElementById("sidebarUserName").textContent = fullName;
    document.getElementById("sidebarUserRole").textContent = currentUser.role;
    document.getElementById("sidebarUserAvatar").textContent = initials;
}

function toggleModuleMenu(id) {
    const menu = document.getElementById(id);
    if (menu) menu.classList.toggle("open");

    if (window.lucide) {
        lucide.createIcons();
    }
}

function getRestrictedSections() {
    return {
        ADMIN: [],

        RECEPCIONISTA: [
            "finances",
            "reports",
            "promotions",
            "auditLogs",
            "users",
            "psychologists",
            "services",
            "availability"
        ],

        PSICOLOGO: [
            "patients",
            "leads",
            "finances",
            "reports",
            "promotions",
            "auditLogs",
            "users",
            "psychologists",
            "services",
            "availability"
        ]
    };
}

function showSectionById(id) {
    const role = currentUser?.role;
    const restrictedSections = getRestrictedSections();

    if (restrictedSections[role]?.includes(id)) {
        Swal.fire("Acceso denegado", "No tienes permiso para acceder a este módulo", "warning");

        if (typeof closeMobileSidebar === "function") {
            closeMobileSidebar();
        }

        return;
    }

    document.querySelectorAll(".section").forEach(section => {
        section.classList.remove("active");
    });

    const target = document.getElementById(id);
    if (target) target.classList.add("active");

    document.querySelectorAll(".sidebar-link").forEach(btn => {
        btn.classList.remove("active");
    });

const activeButton = [...document.querySelectorAll(".sidebar-link")]
    .find(btn => {
        const isVisible = btn.offsetParent !== null;
        const matchesSection =
            btn.getAttribute("onclick")?.includes(`'${id}'`);

        return isVisible && matchesSection;
    });

    if (activeButton) activeButton.classList.add("active");

    if (typeof loadLeads === "function" && id === "leads") {
        loadLeads();
    }

    if (typeof loadDashboard === "function" && id === "home") {
        loadDashboard();
    }

    if (typeof loadAppointments === "function" && id === "appointments") {
        loadAppointments();
    }

    if (typeof closeMobileSidebar === "function") {
        closeMobileSidebar();
    }
}

function applySectionSecurity() {
    if (!currentUser) return;

    const role = currentUser.role;
    const restrictedSections = getRestrictedSections();
    const blocked = restrictedSections[role] || [];

    blocked.forEach(id => {
        const section = document.getElementById(id);
        if (section) section.classList.add("hidden");
    });
}

function applyRoleTheme() {
    if (!currentUser) return;

    document.body.classList.remove(
        "role-admin",
        "role-recepcion",
        "role-psicologo"
    );

    if (currentUser.role === "ADMIN") {
        document.body.classList.add("role-admin");
    }

    if (currentUser.role === "RECEPCIONISTA") {
        document.body.classList.add("role-recepcion");
    }

    if (currentUser.role === "PSICOLOGO") {
        document.body.classList.add("role-psicologo");
    }
}

function toggleMobileSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const overlay = document.querySelector(".sidebar-overlay");

    if (!sidebar || !overlay) return;

    sidebar.classList.toggle("mobile-open");
    overlay.classList.toggle("active");
}

function closeMobileSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const overlay = document.querySelector(".sidebar-overlay");

    if (!sidebar || !overlay) return;

    sidebar.classList.remove("mobile-open");
    overlay.classList.remove("active");
}