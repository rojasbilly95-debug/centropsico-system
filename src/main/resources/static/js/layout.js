async function loadSidebar() {
    const res = await fetch("/components/sidebar.html");
    const html = await res.text();
    document.getElementById("sidebar-container").innerHTML = html;

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
        RECEPCIONISTA: ["finances", "reports", "users", "psychologists", "services"],
        PSICOLOGO: ["patients", "finances", "reports", "users", "psychologists", "services"]
    };
}

function showSectionById(id) {
    const role = currentUser?.role;
    const restrictedSections = getRestrictedSections();

    if (restrictedSections[role]?.includes(id)) {
        Swal.fire("Acceso denegado", "No tienes permiso para acceder a este módulo", "warning");
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
        .find(btn => btn.getAttribute("onclick")?.includes(`'${id}'`));

    if (activeButton) activeButton.classList.add("active");
}

function applyRoleVisibility() {
    if (!currentUser) return;

    if (currentUser.role !== "ADMIN") {
        document.querySelectorAll(".admin-only").forEach(el => {
            el.style.display = "none";
        });
    }

    if (currentUser.role === "PSICOLOGO") {
        document.querySelectorAll(".recepcion-only").forEach(el => {
            el.style.display = "none";
        });
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