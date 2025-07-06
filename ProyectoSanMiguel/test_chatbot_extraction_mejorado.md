# Test de Extracción de Nombres de Complejos - Versión Mejorada

## Casos de Prueba con Nombres Reales

### Complejos en la Base de Datos:
1. Polideportivo Maranga
2. Parque Juan Pablo II
3. Complejo Deportivo Pando
4. Parque Media Luna
5. Complejo Deportivo Arboleda
6. Cancha Deportiva Costanera
7. Gimnasio Municipal
8. Polideportivo La Paz
9. Campo Deportivo Pando Norte
10. Skatepark San Miguel

### Casos de Prueba para Listar Instancias

| Input | Nombre Esperado | Coincidencia Esperada |
|-------|----------------|---------------------|
| "instancias del polideportivo maranga" | "polideportivo maranga" | Exacta |
| "instancias del maranga" | "maranga" | Búsqueda por palabra |
| "instancias del complejo deportivo pando" | "pando" | Filtra "complejo deportivo" |
| "canchas del parque juan pablo" | "parque juan pablo" | Por palabras (sin "II") |
| "instalaciones del gimnasio municipal" | "gimnasio municipal" | Exacta |
| "instancias pando" | "pando" | Sin "del" |
| "instancias skatepark san miguel" | "skatepark san miguel" | Sin "del" |

### Casos de Prueba para Consultar Disponibilidad

| Input | Nombre Esperado | Coincidencia Esperada |
|-------|----------------|---------------------|
| "disponibilidad del complejo deportivo pando para mañana" | "pando" | Búsqueda por palabra |
| "horarios del polideportivo maranga para hoy" | "polideportivo maranga" | Exacta |
| "cancha de futbol del parque media luna para el lunes" | "parque media luna" | Exacta |
| "gimnasio del municipal para mañana" | "municipal" | Búsqueda por palabra |

## Casos de Prueba con Nombres Reales

## Casos Límite

| Input | Resultado Esperado | Explicación |
|-------|-------------------|-------------|
| "instancias" | "" (vacío) | Sin nombre de complejo |
| "instancias del" | "" (vacío) | "del" sin continuación |
| "instancias del complejo" | "" (vacío) | Solo palabra genérica |
| "instancias del complejo deportivo" | "" (vacío) | Solo palabras genéricas |

## Validación de Búsqueda

Los nombres extraídos deben ser buscados con:
1. **Coincidencia exacta** (prioridad alta)
2. **Coincidencia por palabras** (80% de palabras importantes)
3. **Coincidencia permisiva** (al menos una palabra > 3 caracteres)

## Notas de Implementación

### Mejoras Realizadas
1. **Lógica simplificada**: Se eliminaron las expresiones regulares complejas por un enfoque más directo
2. **Manejo de contexto**: Se distingue entre listar instancias y consultar disponibilidad
3. **Búsqueda progresiva**: Primero busca "del/de", luego patrones específicos, finalmente cualquier nombre posible
4. **Limpieza mejorada**: Elimina palabras genéricas pero mantiene nombres específicos

### Palabras que se Filtran
- "complejo", "deportivo", "municipal" (cuando son descriptivas)
- "instancias", "instalaciones", "canchas" (acciones)
- "disponibilidad", "horarios", "precios" (consultas)
- Artículos: "el", "la", "los", "las"
- Preposiciones: "del", "de", "para", "en", "con"

### Palabras que se Mantienen
- Nombres propios o específicos del complejo
- Términos que identifican el complejo de manera única
- Números o códigos si forman parte del nombre

## Tests Recomendados

Para validar completamente la función, se debe probar con:
1. Los nombres reales de complejos en la base de datos
2. Variaciones comunes que escribirían los usuarios
3. Casos con errores tipográficos menores
4. Combinaciones de mayúsculas y minúsculas
