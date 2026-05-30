const tokenKey = "festival-manager-token";
let token = localStorage.getItem(tokenKey);
let activeEntity = "festivals";
let editingId = null;
let cachedData = {};
let currentUser = {username: "", roles: []};
let signUpAfterLoginEventId = null;

// --- CONFIGURATION ---

const roleAccess = {
    ADMIN: {
        label: "Администратор",
        tabs: ["festivals", "venues", "rooms", "eventTypes", "events", "participants", "eventParticipants", "participantTypes", "users", "roles"],
        write: ["festivals", "venues", "rooms", "eventTypes", "events", "participants", "eventParticipants", "participantTypes", "users", "roles"]
    },
    ORGANIZER: {
        label: "Организатор",
        tabs: ["festivals", "venues", "rooms", "eventTypes", "events", "participants", "eventParticipants", "participantTypes"],
        write: ["festivals", "venues", "rooms", "eventTypes", "events", "participants", "eventParticipants", "participantTypes"]
    },
    MANAGER: {
        label: "Менеджер",
        tabs: ["events", "participants", "eventParticipants"],
        write: ["events", "participants", "eventParticipants"]
    },
    USER: {
        label: "Пользователь",
        tabs: ["festivals", "venues", "rooms", "eventTypes", "events"],
        write: []
    }
};

const relationLabels = {
    // Relations
    festival: "Фестиваль",
    venue: "Площадка",
    room: "Помещение",
    eventType: "Тип",
    manager: "Менеджер",
    role: "Роль",
    participantType: "Тип участника",
    event: "Мероприятие",
    participant: "Участник",

    // Common Fields
    id: "ID",
    name: "Название",
    fullName: "ФИО",
    username: "Логин",
    email: "Email",
    phone: "Телефон",
    status: "Статус",
    city: "Город",
    startsOn: "Дата начала",
    endsOn: "Дата окончания",
    address: "Адрес",
    capacity: "Вместимость",
    floor: "Этаж",
    eventDate: "Дата",
    startsAt: "Начало",
    endsAt: "Окончание",
    roleAtEvent: "Роль на мероприятии",
    registrationTime: "Время регистрации",
    conflictCheckRequired: "Проверка конфликтов",
    permissionDescription: "Описание прав",
    birthDate: "Дата рождения",
    photoUrl: "Фото",
    note: "Примечание",
    equipment: "Оборудование",
    description: "Описание"
};

const entities = {
    festivals: {
        title: "Фестивали",
        endpoint: "/api/festivals",
        columns: ["id", "name", "city", "startsOn", "endsOn", "status"],
        fields: [
            {name: "name", label: "Название", required: true},
            {name: "city", label: "Город", required: true},
            {name: "startsOn", label: "Дата начала", type: "date", required: true},
            {name: "endsOn", label: "Дата окончания", type: "date", required: true},
            {name: "status", label: "Статус", type: "select", options: ["PLANNED", "RUNNING", "FINISHED", "CANCELLED"]}
        ]
    },
    venues: {
        title: "Площадки",
        endpoint: "/api/venues",
        columns: ["id", "festival", "name", "city", "address", "status"],
        fields: [
            {name: "festival", label: "Фестиваль", type: "relation", entity: "festivals", required: true},
            {name: "name", label: "Название", required: true},
            {name: "city", label: "Город", required: true},
            {name: "address", label: "Адрес", required: true},
            {name: "status", label: "Статус", type: "select", options: ["ACTIVE", "UNAVAILABLE"]},
            {name: "note", label: "Примечание", type: "textarea"}
        ]
    },
    rooms: {
        title: "Помещения",
        endpoint: "/api/rooms",
        columns: ["id", "venue", "name", "capacity", "floor"],
        fields: [
            {name: "venue", label: "Площадка", type: "relation", entity: "venues", required: true},
            {name: "name", label: "Название", required: true},
            {name: "capacity", label: "Вместимость", type: "number", required: true},
            {name: "floor", label: "Этаж", type: "number"},
            {name: "equipment", label: "Оборудование", type: "textarea"}
        ]
    },
    eventTypes: {
        title: "Типы мероприятий",
        endpoint: "/api/event-types",
        columns: ["id", "name"],
        fields: [{name: "name", label: "Название", required: true}]
    },
    events: {
        title: "Мероприятия",
        endpoint: "/api/events",
        columns: ["id", "festival", "room", "eventType", "manager", "name", "eventDate", "startsAt", "endsAt", "status"],
        fields: [
            {name: "festival", label: "Фестиваль", type: "relation", entity: "festivals", required: true},
            {name: "room", label: "Помещение", type: "relation", entity: "rooms", required: true},
            {name: "eventType", label: "Тип", type: "relation", entity: "eventTypes", required: true},
            {name: "manager", label: "Менеджер", type: "relation", entity: "users", required: true},
            {name: "name", label: "Название", required: true},
            {name: "description", label: "Описание", type: "textarea"},
            {name: "eventDate", label: "Дата", type: "date", required: true},
            {name: "startsAt", label: "Начало", type: "time", required: true},
            {name: "endsAt", label: "Окончание", type: "time", required: true},
            {name: "status", label: "Статус", type: "select", options: ["PLANNED", "RUNNING", "FINISHED", "CANCELLED"]}
        ]
    },
    participants: {
        title: "Участники",
        endpoint: "/api/participants",
        columns: ["id", "participantType", "fullName", "phone", "email", "city"],
        fields: [
            {name: "participantType", label: "Тип участника", type: "relation", entity: "participantTypes", required: true},
            {name: "fullName", label: "ФИО", required: true},
            {name: "birthDate", label: "Дата рождения", type: "date"},
            {name: "phone", label: "Телефон", required: true, placeholder: "79991112233"},
            {name: "email", label: "Email", type: "email", required: true},
            {name: "city", label: "Город"},
            {name: "photoUrl", label: "Фото"},
            {name: "note", label: "Примечание", type: "textarea"}
        ]
    },
    eventParticipants: {
        title: "Участники мероприятий",
        endpoint: "/api/event-participants",
        columns: ["id", "event", "participant", "roleAtEvent", "status", "registrationTime"],
        fields: [
            {name: "event", label: "Мероприятие", type: "relation", entity: "events", required: true},
            {name: "participant", label: "Участник", type: "relation", entity: "participants", required: true},
            {name: "roleAtEvent", label: "Роль на мероприятии", required: true},
            {name: "status", label: "Статус", type: "select", options: ["INVITED", "CONFIRMED", "DECLINED", "ATTENDED"]},
            {name: "registrationTime", label: "Время регистрации", type: "datetime-local"}
        ]
    },
    participantTypes: {
        title: "Типы участников",
        endpoint: "/api/participant-types",
        columns: ["id", "name", "conflictCheckRequired"],
        fields: [
            {name: "name", label: "Название", required: true},
            {name: "conflictCheckRequired", label: "Проверять пересечения", type: "checkbox"}
        ]
    },
    users: {
        title: "Пользователи",
        endpoint: "/api/users",
        columns: ["id", "role", "fullName", "username", "email", "phone", "status"],
        fields: [
            {name: "role", label: "Роль", type: "relation", entity: "roles", required: true},
            {name: "fullName", label: "ФИО", required: true},
            {name: "username", label: "Логин", required: true},
            {name: "password", label: "Пароль", type: "password", required: true},
            {name: "email", label: "Email", type: "email", required: true},
            {name: "phone", label: "Телефон", required: true, placeholder: "79991112233"},
            {name: "status", label: "Статус", type: "select", options: ["ACTIVE", "BLOCKED"]}
        ]
    },
    roles: {
        title: "Роли",
        endpoint: "/api/roles",
        columns: ["id", "name", "permissionDescription"],
        fields: [
            {name: "name", label: "Название", required: true},
            {name: "permissionDescription", label: "Описание прав", type: "textarea"}
        ]
    }
};

// --- INITIALIZATION & EVENT BINDING ---

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    if (token) {
        currentUser = readUserFromToken(token);
        showApp();
    } else {
        initLandingPage();
    }
});

function bindEvents() {
    // Main App
    document.getElementById("loginForm").addEventListener("submit", login);
    document.getElementById("logoutButton").addEventListener("click", logout);
    document.getElementById("newButton").addEventListener("click", () => renderForm());
    document.getElementById("entityForm").addEventListener("submit", saveEntity);

    // Landing Page
    document.getElementById("showLoginButton").addEventListener("click", () => showLogin());
    document.querySelectorAll(".landing-nav a").forEach(anchor => {
        anchor.addEventListener("click", (e) => {
            e.preventDefault();
            const targetId = anchor.getAttribute("href");
            document.querySelector(targetId).scrollIntoView({ behavior: "smooth" });
        });
    });
    document.getElementById("festivalsList").addEventListener("click", (e) => {
        if (e.target.matches("button")) {
            document.getElementById("eventsList").scrollIntoView({ behavior: "smooth" });
        }
    });
    document.getElementById("eventsList").addEventListener("click", handleEventSignUp);
}

// --- LANDING PAGE LOGIC ---

async function initLandingPage() {
    try {
        const festivals = await (await api("/api/festivals")).json();
        const events = await (await api("/api/events")).json();
        renderFestivals(festivals);
        renderEvents(events);
    } catch (error) {
        console.error("Failed to load landing page data:", error);
        const festivalsContainer = document.getElementById("festivalsList");
        if(festivalsContainer) {
            festivalsContainer.innerHTML = "<p>Не удалось загрузить данные. Попробуйте обновить страницу.</p>";
        }
    }
}

function renderFestivals(festivals) {
    const container = document.getElementById("festivalsList");
    if (!container) return;
    if (!festivals.length) {
        container.innerHTML = "<p>Скоро здесь появятся фестивали.</p>";
        return;
    }
    container.innerHTML = festivals.map(festival => `
        <article class="item-card">
            <div class="item-card-header">
                <h3>${escapeHtml(festival.name)}</h3>
            </div>
            <div class="item-card-body">
                <p><strong>Город:</strong> ${escapeHtml(festival.city)}</p>
                <p><strong>Даты:</strong> ${formatValue(festival.startsOn)} - ${formatValue(festival.endsOn)}</p>
            </div>
            <div class="item-card-footer">
                <button data-festival-id="${festival.id}">Подробнее</button>
            </div>
        </article>
    `).join("");
}

function renderEvents(events) {
    const container = document.getElementById("eventsList");
    if (!container) return;
    if (!events.length) {
        container.innerHTML = "<p>Мероприятий пока не запланировано.</p>";
        return;
    }
    container.innerHTML = events.slice(0, 3).map(event => `
        <article class="item-card">
            <div class="item-card-header">
                <h3>${escapeHtml(event.name)}</h3>
            </div>
            <div class="item-card-body">
                <p><strong>Дата:</strong> ${formatValue(event.eventDate)}</p>
                <p><strong>Время:</strong> ${formatValue(event.startsAt)} - ${formatValue(event.endsAt)}</p>
                <p><strong>Тип:</strong> ${escapeHtml(event.eventType?.name || "-")}</p>
            </div>
            <div class="item-card-footer">
                <button data-event-id="${event.id}">Записаться</button>
            </div>
        </article>
    `).join("");
}

async function handleEventSignUp(e) {
    if (!e.target.matches("button")) return;
    const button = e.target;
    const eventId = button.dataset.eventId;

    if (!token) {
        signUpAfterLoginEventId = eventId;
        showLogin("Чтобы записаться на мероприятие, пожалуйста, войдите в систему.");
        return;
    }

    button.disabled = true;
    button.textContent = "Записываем...";

    try {
        await api(`/api/events/${eventId}/register`, { method: "POST" });
        button.textContent = "Вы записаны!";
        button.classList.add("success");
    } catch (error) {
        alert(`Ошибка записи: ${error.message}`);
        button.disabled = false;
        button.textContent = "Записаться";
    }
}

// --- APP & AUTHENTICATION LOGIC ---

function buildNavigation() {
    const nav = document.getElementById("entityNav");
    nav.innerHTML = "";
    allowedTabs().forEach((key) => {
        const config = entities[key];
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = config.title;
        button.dataset.entity = key;
        button.addEventListener("click", () => selectEntity(key));
        nav.appendChild(button);
    });
}

function showLogin(message = "") {
    if (!message) { // Clear the pending sign-up if login is initiated from somewhere else
        signUpAfterLoginEventId = null;
    }
    setLoginMessage(message);
    document.getElementById("landingView").classList.add("hidden");
    document.getElementById("mainApp").classList.remove("hidden");
    document.getElementById("loginView").classList.remove("hidden");
    document.getElementById("appView").classList.add("hidden");
}

async function login(event) {
    event.preventDefault();
    setLoginMessage("");
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({username, password})
        });
        if (!response.ok) {
            throw new Error("Неверный логин или пароль.");
        }
        const data = await response.json();
        token = data.token;
        currentUser = readUserFromToken(token);
        localStorage.setItem(tokenKey, token);

        if (signUpAfterLoginEventId) {
            const eventIdToSignUp = signUpAfterLoginEventId;
            signUpAfterLoginEventId = null;
            
            try {
                await api(`/api/events/${eventIdToSignUp}/register`, { method: "POST" });
            } catch(regError) {
                 alert(`Ошибка записи: ${regError.message}`);
            }

            document.getElementById("mainApp").classList.add("hidden");
            document.getElementById("landingView").classList.remove("hidden");
            await initLandingPage();
            
            const justRegisteredCard = document.querySelector(`button[data-event-id="${eventIdToSignUp}"]`);
            if (justRegisteredCard) {
                 justRegisteredCard.closest('.item-card').scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } else {
            await showApp();
        }
    } catch (error) {
        setLoginMessage(error.message);
    }
}

function logout() {
    token = null;
    cachedData = {};
    currentUser = {username: "", roles: []};
    signUpAfterLoginEventId = null; // Clear on logout
    localStorage.removeItem(tokenKey);
    document.getElementById("mainApp").classList.add("hidden");
    document.getElementById("landingView").classList.remove("hidden");
    initLandingPage();
}

async function showApp() {
    const tabs = allowedTabs();
    if (!tabs.includes(activeEntity)) {
        activeEntity = tabs[0] || "festivals";
    }
    buildNavigation();
    document.getElementById("currentUserLabel").textContent = `${currentUser.username || "user"} · ${currentRoleLabel()}`;
    document.getElementById("landingView").classList.add("hidden");
    document.getElementById("mainApp").classList.remove("hidden");
    document.getElementById("loginView").classList.add("hidden");
    document.getElementById("appView").classList.remove("hidden");
    await selectEntity(activeEntity);
    await refreshStats();
}

// --- CORE APP RENDERING ---

async function selectEntity(entityKey) {
    if (!allowedTabs().includes(entityKey)) {
        entityKey = allowedTabs()[0];
    }
    activeEntity = entityKey;
    editingId = null;
    document.querySelectorAll(".entity-nav button").forEach((button) => {
        button.classList.toggle("active", button.dataset.entity === entityKey);
    });
    const config = entities[entityKey];
    document.getElementById("entityTitle").textContent = config.title;
    document.getElementById("entitySubtitle").textContent = config.endpoint;
    document.getElementById("newButton").classList.toggle("hidden", !canWrite(entityKey));
    await loadEntity(entityKey);
    renderTable(entityKey);
    await renderForm();
}

async function refreshStats() {
    const stats = [
        ["festivals", "festivalCount"],
        ["events", "eventCount"],
        ["participants", "participantCount"],
        ["rooms", "roomCount"]
    ];
    for (const [entityKey, elementId] of stats) {
        try {
            const data = await loadEntity(entityKey);
            document.getElementById(elementId).textContent = data.length;
        } catch {
            document.getElementById(elementId).textContent = "0";
        }
    }
}

async function loadEntity(entityKey, force = false) {
    if (!force && cachedData[entityKey]) {
        return cachedData[entityKey];
    }
    const response = await api(entities[entityKey].endpoint);
    cachedData[entityKey] = await response.json();
    return cachedData[entityKey];
}

function renderTable(entityKey) {
    const config = entities[entityKey];
    const rows = cachedData[entityKey] || [];
    const wrap = document.getElementById("tableWrap");
    const writable = canWrite(entityKey);
    if (!rows.length) {
        wrap.innerHTML = '<div class="empty-state">Записей пока нет</div>';
        return;
    }

    const head = config.columns.map((column) => `<th>${columnLabel(column)}</th>`).join("");
    const body = rows.map((row) => {
        const cells = config.columns.map((column) => `<td>${formatValue(row[column])}</td>`).join("");
        const actions = writable ? `
            <td class="actions">
                <button class="ghost" type="button" onclick="editEntity(${row.id})">Изменить</button>
                <button class="danger" type="button" onclick="deleteEntity(${row.id})">Удалить</button>
            </td>
        ` : "";
        return `
            <tr>
                ${cells}
                ${actions}
            </tr>
        `;
    }).join("");

    wrap.innerHTML = `
        <table>
            <thead><tr>${head}${writable ? "<th>Действия</th>" : ""}</tr></thead>
            <tbody>${body}</tbody>
        </table>
    `;
}

async function renderForm(row = null) {
    editingId = row?.id || null;
    const config = entities[activeEntity];
    document.getElementById("formTitle").textContent = editingId ? "Редактирование записи" : "Создание записи";
    setFormMessage("");

    if (!canWrite(activeEntity)) {
        document.getElementById("formTitle").textContent = "Только просмотр";
        document.getElementById("entityForm").innerHTML = `
            <div class="readonly-note">
                У вашей роли нет прав на создание, изменение и удаление записей в этом разделе.
            </div>
        `;
        return;
    }

    await loadRelations(config);

    const fields = config.fields.map((field) => renderField(field, row)).join("");
    document.getElementById("entityForm").innerHTML = `
        ${fields}
        <div class="form-actions">
            <button type="submit">${editingId ? "Сохранить" : "Создать"}</button>
            <button class="ghost" type="button" onclick="renderForm()">Очистить</button>
        </div>
    `;
}

async function loadRelations(config) {
    const relationFields = config.fields.filter((field) => field.type === "relation");
    for (const field of relationFields) {
        await loadEntity(field.entity);
    }
}

function renderField(field, row) {
    const value = fieldValue(field, row);
    if (field.type === "textarea") {
        return `
            <label class="form-row">
                ${field.label}
                <textarea name="${field.name}" ${field.required ? "required" : ""}>${escapeHtml(value)}</textarea>
            </label>
        `;
    }
    if (field.type === "select") {
        const options = field.options.map((option) => {
            const selected = value === option ? "selected" : "";
            return `<option value="${option}" ${selected}>${option}</option>`;
        }).join("");
        return `
            <label class="form-row">
                ${field.label}
                <select name="${field.name}" ${field.required ? "required" : ""}>${options}</select>
            </label>
        `;
    }
    if (field.type === "relation") {
        const options = (cachedData[field.entity] || []).map((item) => {
            const selected = Number(value) === Number(item.id) ? "selected" : "";
            return `<option value="${item.id}" ${selected}>${escapeHtml(entityLabel(item))}</option>`;
        }).join("");
        return `
            <label class="form-row">
                ${field.label}
                <select name="${field.name}" ${field.required ? "required" : ""}>
                    <option value="">Выберите</option>
                    ${options}
                </select>
            </label>
        `;
    }
    if (field.type === "checkbox") {
        return `
            <label class="checkbox-row">
                <input name="${field.name}" type="checkbox" ${value ? "checked" : ""}>
                ${field.label}
            </label>
        `;
    }
    return `
        <label class="form-row">
            ${field.label}
            <input name="${field.name}" type="${field.type || "text"}" value="${escapeHtml(value)}"
                   ${field.placeholder ? `placeholder="${field.placeholder}"` : ""}
                   ${field.required ? "required" : ""}>
        </label>
    `;
}

function fieldValue(field, row) {
    if (!row) {
        if (field.type === "checkbox") {
            return false;
        }
        return field.options?.[0] || "";
    }
    if (field.type === "relation") {
        return row[field.name]?.id || "";
    }
    if (field.type === "datetime-local" && row[field.name]) {
        return row[field.name].slice(0, 16);
    }
    return row[field.name] ?? "";
}

async function saveEntity(event) {
    event.preventDefault();
    if (!canWrite(activeEntity)) {
        setFormMessage("Недостаточно прав для изменения данных.");
        return;
    }
    const config = entities[activeEntity];
    const form = new FormData(event.currentTarget);
    const payload = {};

    config.fields.forEach((field) => {
        if (field.type === "checkbox") {
            payload[field.name] = form.has(field.name);
            return;
        }
        const raw = form.get(field.name);
        if (field.type === "relation") {
            payload[field.name] = raw ? {id: Number(raw)} : null;
            return;
        }
        if (field.type === "number") {
            payload[field.name] = raw === "" ? null : Number(raw);
            return;
        }
        payload[field.name] = raw === "" ? null : raw;
    });

    try {
        const url = editingId ? `${config.endpoint}/${editingId}` : config.endpoint;
        const method = editingId ? "PUT" : "POST";
        const response = await api(url, {
            method,
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });
        const saved = await response.json();
        setFormMessage(`Запись #${saved.id} сохранена.`, true);
        await loadEntity(activeEntity, true);
        renderTable(activeEntity);
        await refreshStats();
        renderForm();
    } catch (error) {
        setFormMessage(error.message);
    }
}

async function editEntity(id) {
    if (!canWrite(activeEntity)) {
        return;
    }
    const rows = cachedData[activeEntity] || [];
    const row = rows.find((item) => Number(item.id) === Number(id));
    if (row) {
        await renderForm(row);
    }
}

async function deleteEntity(id) {
    if (!canWrite(activeEntity)) {
        setFormMessage("Недостаточно прав для удаления данных.");
        return;
    }
    if (!confirm("Удалить запись?")) {
        return;
    }
    try {
        await api(`${entities[activeEntity].endpoint}/${id}`, {method: "DELETE"});
        await loadEntity(activeEntity, true);
        renderTable(activeEntity);
        renderForm();
        await refreshStats();
    } catch (error) {
        setFormMessage(error.message);
    }
}

// --- UTILITIES ---

async function api(url, options = {}) {
    const headers = options.headers ? {...options.headers} : {};
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    const response = await fetch(url, {...options, headers});
    if (response.status === 401) {
        logout();
        throw new Error("Нужен вход в систему.");
    }
    if (response.status === 403) {
        throw new Error("Недостаточно прав для этого действия.");
    }
    if (!response.ok) {
        let message = `Ошибка ${response.status}`;
        try {
            const error = await response.json();
            message = error.message || message;
        } catch {
            message = await response.text();
        }
        throw new Error(message);
    }
    return response;
}

function readUserFromToken(jwt) {
    try {
        const base64 = jwt.split(".")[1].replaceAll("-", "+").replaceAll("_", "/");
        const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, "=");
        const bytes = Uint8Array.from(atob(padded), (char) => char.charCodeAt(0));
        const payload = JSON.parse(new TextDecoder().decode(bytes));
        const roles = (payload.roles || []).map((role) => role.replace("ROLE_", ""));
        return {username: payload.sub || "", roles};
    } catch {
        return {username: "", roles: ["USER"]};
    }
}

function primaryRole() {
    const priority = ["ADMIN", "ORGANIZER", "MANAGER", "USER"];
    return priority.find((role) => currentUser.roles.includes(role)) || "USER";
}

function currentRoleLabel() {
    return roleAccess[primaryRole()]?.label || "Пользователь";
}

function allowedTabs() {
    return roleAccess[primaryRole()]?.tabs || roleAccess.USER.tabs;
}

function canWrite(entityKey) {
    return roleAccess[primaryRole()]?.write.includes(entityKey) || false;
}

function formatValue(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    if (typeof value === "object") {
        return escapeHtml(entityLabel(value));
    }
    if (typeof value === "boolean") {
        return value ? "Да" : "Нет";
    }
    return escapeHtml(String(value));
}

function entityLabel(item) {
    return item.name || item.fullName || item.username || `#${item.id}`;
}

function columnLabel(column) {
    return relationLabels[column] || column;
}

function setLoginMessage(message) {
    document.getElementById("loginMessage").textContent = message;
}

function setFormMessage(message, success = false) {
    const element = document.getElementById("formMessage");
    element.textContent = message;
    element.classList.toggle("success", success);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
