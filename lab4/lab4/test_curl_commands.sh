#!/bin/bash

# Тестовые curl команды для lab4 Mule API
# Базовый URL
BASE_URL="https://localhost:55556/api/v1"

echo "========================================="
echo "Тестирование Mule API Endpoints"
echo "========================================="
echo ""

# 1. GET /routes - Получить все маршруты с пагинацией
echo "1. GET /routes - Получить все маршруты (страница 1, размер 10)"
curl -k -X GET "${BASE_URL}/routes?page=1&pageSize=10&sort=id_asc" \
  -H "Accept: application/xml" \
  -H "Content-Type: application/xml"
echo -e "\n\n"

# 2. GET /routes с фильтрами
echo "2. GET /routes - С фильтром по имени"
curl -k -X GET "${BASE_URL}/routes?name=Test&name_op=~&page=1&pageSize=10" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 3. POST /routes - Создать новый маршрут (успех)
echo "3. POST /routes - Создать новый маршрут"
curl -k -X POST "${BASE_URL}/routes" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Route>
  <name>TestRoute123</name>
  <coordinates>
    <x>100</x>
    <y>200</y>
  </coordinates>
  <from>
    <x>10.0</x>
    <y>20.0</y>
    <name>StartPoint</name>
  </from>
  <to>
    <x>30.0</x>
    <y>40.0</y>
    <name>EndPoint</name>
  </to>
  <distance>500</distance>
  <priority>MEDIUM</priority>
</Route>'
echo -e "\n\n"

# 4. POST /routes - Создать маршрут с ошибкой валидации (distance < 1)
echo "4. POST /routes - Ошибка валидации (distance < 1)"
curl -k -X POST "${BASE_URL}/routes" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Route>
  <name>InvalidRoute</name>
  <coordinates>
    <x>100</x>
    <y>200</y>
  </coordinates>
  <from>
    <x>10.0</x>
    <y>20.0</y>
    <name>Start</name>
  </from>
  <to>
    <x>30.0</x>
    <y>40.0</y>
    <name>End</name>
  </to>
  <distance>0</distance>
</Route>'
echo -e "\n\n"

# 5. GET /routes/{id} - Получить маршрут по ID (существующий)
echo "5. GET /routes/2 - Получить маршрут по ID"
curl -k -X GET "${BASE_URL}/routes/2" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 6. GET /routes/{id} - Ошибка 404 (несуществующий ID)
echo "6. GET /routes/99999 - Ошибка 404 (несуществующий ID)"
curl -k -X GET "${BASE_URL}/routes/99999" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 7. PUT /routes/{id} - Обновить маршрут (успех)
echo "7. PUT /routes/5 - Обновить маршрут"
curl -k -X PUT "${BASE_URL}/routes/5" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Route>
  <name>UpdatedRoute</name>
  <coordinates>
    <x>150</x>
    <y>250</y>
  </coordinates>
  <from>
    <x>15.0</x>
    <y>25.0</y>
    <name>NewStart</name>
  </from>
  <to>
    <x>35.0</x>
    <y>45.0</y>
    <name>NewEnd</name>
  </to>
  <distance>600</distance>
  <priority>HIGH</priority>
</Route>'
echo -e "\n\n"

# 8. PUT /routes/{id} - Ошибка 404 (несуществующий ID)
echo "8. PUT /routes/99999 - Ошибка 404 (несуществующий ID)"
curl -k -X PUT "${BASE_URL}/routes/99999" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Route>
  <name>NoRoute</name>
  <coordinates>
    <x>1</x>
    <y>1</y>
  </coordinates>
  <from>
    <x>1.0</x>
    <y>1.0</y>
    <name>A</name>
  </from>
  <to>
    <x>2.0</x>
    <y>2.0</y>
    <name>B</name>
  </to>
  <distance>10</distance>
</Route>'
echo -e "\n\n"

# 9. PUT /routes/{id} - Ошибка 422 (невалидные данные)
echo "9. PUT /routes/2 - Ошибка 422 (distance < 1)"
curl -k -X PUT "${BASE_URL}/routes/2" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<Route>
  <name>InvalidUpdate</name>
  <coordinates>
    <x>1</x>
    <y>1</y>
  </coordinates>
  <from>
    <x>1.0</x>
    <y>1.0</y>
    <name>A</name>
  </from>
  <to>
    <x>2.0</x>
    <y>2.0</y>
    <name>B</name>
  </to>
  <distance>-5</distance>
</Route>'
echo -e "\n\n"

# 10. DELETE /routes/{id} - Удалить маршрут (успех)
echo "10. DELETE /routes/10 - Удалить маршрут (если существует)"
curl -k -X DELETE "${BASE_URL}/routes/10" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 11. DELETE /routes/{id} - Ошибка 404 (несуществующий ID)
echo "11. DELETE /routes/99999 - Ошибка 404 (несуществующий ID)"
curl -k -X DELETE "${BASE_URL}/routes/99999" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 12. GET /routes/max/from - Получить маршрут с максимальным from
echo "12. GET /routes/max/from - Получить маршрут с максимальным from"
curl -k -X GET "${BASE_URL}/routes/max/from" \
  -H "Accept: application/xml"
echo -e "\n\n"

# 13. GET /routes/distances/unique - Получить уникальные расстояния
echo "13. GET /routes/distances/unique - Получить уникальные расстояния"
curl -k -X GET "${BASE_URL}/routes/distances/unique" \
  -H "Accept: application/xml"
echo -e "\n\n"

echo "========================================="
echo "Тестирование завершено"
echo "========================================="

