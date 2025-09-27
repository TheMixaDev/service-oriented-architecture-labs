document.addEventListener('DOMContentLoaded', () => {
    const routesApiBaseUrl = 'http://localhost:27275/routes-service/api/v1/routes';
    const navigatorApiBaseUrl = 'http://localhost:27274/navigator-service/api/v1/navigator';

    const routesTbody = document.querySelector('#routes-table tbody');
    const routeForm = document.getElementById('route-form');
    const applyFiltersBtn = document.getElementById('apply-filters');
    const navigatorOptimalForm = document.getElementById('navigator-optimal-form');
    const navigatorAddForm = document.getElementById('navigator-add-form');

    let currentPage = 1;
    const pageSize = 10;

    async function fetchRoutes(page = 1, filters = {}, sort = 'id_asc') {
        const params = new URLSearchParams({
            page: page,
            pageSize: pageSize,
            sort: sort,
            ...filters
        });

        try {
            const response = await fetch(`${routesApiBaseUrl}?${params}`);
            if (!response.ok) {
                const errorXml = await response.text();
                throw new Error(`HTTP error! status: ${response.status} - ${errorXml}`);
            }

            const totalCount = response.headers.get('X-Total-Count');
            const xmlString = await response.text();
            const routes = parseRoutesXml(xmlString);
            
            renderRoutes(routes);
            renderPagination(totalCount, page, pageSize);

        } catch (error) {
            console.error('Не удалось загрузить маршруты:', error);
            showError('Не удалось загрузить маршруты. Пожалуйста, проверьте соединение с сервисом.');
        }
    }

    function parseRoutesXml(xmlString) {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xmlString, "application/xml");
        const errorNode = xmlDoc.getElementsByTagName('Error')[0];
        if (errorNode) {
            const message = errorNode.getElementsByTagName('message')[0]?.textContent;
            throw new Error(message || 'Неизвестная ошибка сервера');
        }

        return Array.from(xmlDoc.getElementsByTagName('Route')).map(routeNode => {
            const getVal = (context, tagName) => context.getElementsByTagName(tagName)[0]?.textContent || '';
            
            const coordinatesNode = routeNode.getElementsByTagName('coordinates')[0] || {};
            const fromNode = routeNode.getElementsByTagName('from')[0] || {};
            const toNode = routeNode.getElementsByTagName('to')[0] || {};

            return {
                id: getVal(routeNode, 'id'),
                name: getVal(routeNode, 'name'),
                coordinates: `(${getVal(coordinatesNode, 'x')}, ${getVal(coordinatesNode, 'y')})`,
                creationDate: new Date(getVal(routeNode, 'creationDate')).toLocaleString(),
                from: `${getVal(fromNode, 'name')} (${getVal(fromNode, 'x')}, ${getVal(fromNode, 'y')})`,
                to: `${getVal(toNode, 'name')} (${getVal(toNode, 'x')}, ${getVal(toNode, 'y')})`,
                distance: getVal(routeNode, 'distance')
            };
        });
    }

    function renderRoutes(routes) {
        routesTbody.innerHTML = '';
        if (routes.length === 0) {
            routesTbody.innerHTML = '<tr><td colspan="8">Маршруты не найдены.</td></tr>';
            return;
        }
        routes.forEach(route => {
            const row = routesTbody.insertRow();
            row.innerHTML = `
                <td>${route.id}</td>
                <td>${route.name}</td>
                <td>${route.coordinates}</td>
                <td>${route.creationDate}</td>
                <td>${route.from}</td>
                <td>${route.to}</td>
                <td>${route.distance || 'N/A'}</td>
                <td>
                    <button class="edit-btn" data-id="${route.id}">Редактировать</button>
                    <button class="delete-btn" data-id="${route.id}">Удалить</button>
                </td>
            `;
        });
    }

    function renderPagination(totalItems, currentPage, pageSize) {
        const paginationContainer = document.querySelector('.pagination');
        paginationContainer.innerHTML = '';
        const totalPages = Math.ceil(totalItems / pageSize);

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;
            button.addEventListener('click', () => {
                currentPage = i;
                applyFiltersAndSort();
            });
            paginationContainer.appendChild(button);
        }
    }

    function applyFiltersAndSort() {
        const filters = {
            name: document.getElementById('filter-name').value,
            distance: document.getElementById('filter-distance').value
        };
        const sort = document.getElementById('sort-by').value;
        
        const activeFilters = Object.fromEntries(Object.entries(filters).filter(([_, v]) => v));
        fetchRoutes(currentPage, activeFilters, sort);
    }
    
    applyFiltersBtn.addEventListener('click', () => {
        currentPage = 1;
        applyFiltersAndSort();
    });

    routesTbody.addEventListener('click', async (event) => {
        const target = event.target;
        const id = target.dataset.id;
        if (target.classList.contains('delete-btn')) {
            deleteRoute(id);
        }
        if (target.classList.contains('edit-btn')) {
            const route = await getRouteById(id);
            if(route) populateRouteForm(route);
        }
    });

    routeForm.addEventListener('submit', (event) => {
        event.preventDefault();
        saveRoute();
    });

    navigatorOptimalForm.addEventListener('submit', (event) => {
        event.preventDefault();
        findOptimalRoute();
    });

    navigatorAddForm.addEventListener('submit', (event) => {
        event.preventDefault();
        addNavigatorRoute();
    });

    async function getRouteById(id) {
        try {
            const response = await fetch(`${routesApiBaseUrl}/${id}`);
            if (!response.ok) throw new Error('Маршрут не найден');
            const xmlString = await response.text();
            return parseRoutesXml(xmlString)[0];
        } catch (error) {
            showError(error.message);
            return null;
        }
    }

    async function saveRoute() {
        const id = document.getElementById('route-id').value;
        const name = document.getElementById('route-name').value;
        const distance = document.getElementById('route-distance').value;
    
        const payload = `
            <Route>
                <name>${name}</name>
                <coordinates>
                    <x>${document.getElementById('route-coord-x').value}</x>
                    <y>${document.getElementById('route-coord-y').value}</y>
                </coordinates>
                <from>
                    <x>${document.getElementById('route-from-x').value}</x>
                    <y>${document.getElementById('route-from-y').value}</y>
                    <name>${document.getElementById('route-from-name').value}</name>
                </from>
                <to>
                    <x>${document.getElementById('route-to-x').value}</x>
                    <y>${document.getElementById('route-to-y').value}</y>
                    <name>${document.getElementById('route-to-name').value}</name>
                </to>
                ${distance ? `<distance>${distance}</distance>` : ''}
            </Route>
        `;
    
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${routesApiBaseUrl}/${id}` : routesApiBaseUrl;
    
        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/xml' },
                body: payload
            });
    
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Не удалось сохранить маршрут: ${errorText}`);
            }
    
            routeForm.reset();
            fetchRoutes(currentPage);
        } catch (error) {
            showError(error.message);
        }
    }

    async function deleteRoute(id) {
        if (!confirm(`Вы уверены, что хотите удалить маршрут ${id}?`)) return;
        
        try {
            const response = await fetch(`${routesApiBaseUrl}/${id}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error('Не удалось удалить маршрут.');
            }
            fetchRoutes(currentPage);
        } catch (error) {
            showError(error.message);
        }
    }

    async function findOptimalRoute() {
        const fromId = document.getElementById('nav-from-id').value;
        const toId = document.getElementById('nav-to-id').value;
        const shortest = document.getElementById('nav-shortest').value;
        
        try {
            const response = await fetch(`${navigatorApiBaseUrl}/route/${fromId}/${toId}/${shortest}`);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Не удалось найти маршрут: ${errorText}`);
            }
            const xmlString = await response.text();
            const route = parseRoutesXml(xmlString)[0];
            alert(`Оптимальный маршрут найден: ${route.name}, Расстояние: ${route.distance}`);
        } catch (error) {
            showError(error.message);
        }
    }

    async function addNavigatorRoute() {
        const fromId = document.getElementById('nav-add-from-id').value;
        const toId = document.getElementById('nav-add-to-id').value;
        const distance = document.getElementById('nav-add-distance').value;

        try {
            const response = await fetch(`${navigatorApiBaseUrl}/route/add/${fromId}/${toId}/${distance}`, { method: 'POST' });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Не удалось добавить маршрут: ${errorText}`);
            }
            alert('Маршрут добавлен успешно!');
            fetchRoutes();
        } catch (error) {
            showError(error.message);
        }
    }

    function populateRouteForm(route) {
        document.getElementById('route-id').value = route.id;
        document.getElementById('route-name').value = route.name;
        alert(`Редактирование маршрута ID: ${route.id}. Пожалуйста, заполните все поля для обновления.`);
    }

    function showError(message) {
        alert(message);
    }

    fetchRoutes();
});
