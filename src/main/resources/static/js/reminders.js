async function loadReminders() {

    try {

        const response = await fetch("/api/reminders/me", {
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        });

        if (!response.ok) return;

        const reminders = await response.json();

        if (!reminders.length) return;

        renderReminderPanel(reminders);

    } catch (error) {
        console.error("Error cargando recordatorios", error);
    }
}

function renderReminderPanel(reminders) {

    const existing = document.getElementById("reminderPanel");

    if (existing) existing.remove();

    const panel = document.createElement("div");

    panel.id = "reminderPanel";

    panel.innerHTML = `

        <div class="reminder-header">
            <span>🔔 Recordatorios</span>
            <button onclick="closeReminderPanel()">✕</button>
        </div>

        <div class="reminder-body">
            ${reminders.map(reminder => `
                <div class="reminder-item ${reminder.type}">
                    <strong>${reminder.title}</strong>
                    <p>${reminder.message}</p>
                </div>
            `).join("")}
        </div>
    `;

    document.body.appendChild(panel);
}

function closeReminderPanel() {
    document.getElementById("reminderPanel")?.remove();
}