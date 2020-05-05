# restApiAccounts

Данный API предоставляет методы внесения/снятия/перевода денег со счета. Дополнительно добавлен метод, позволяющий создать новый аккаунт по имени владельца, при создании предполагается задание некоторого `initial_amount`. Допускается, что у одного владельца может быть заведено неограниченное количество аккаунтов.

Таблица с аккаунтами содержит упрощенные сущности аккаунтов в виде accountId--ownerName--balance. Для более близкой к реальности задачи можно было добавить поля ownerId, currency, creationDate, lastModifiedDate и прочие. В качестве хранилища используется H2 Database. При запуске приложения, создается таблица accounts и наполняется минимальным начальным набором данных. Для H2 Database доступна веб консоль: `localhost:8080/h2-database`, креденшиалы заданы в `application.properties`.

Реализован ряд тестов, которые имитируют чтение/запись данных в таблицу в несколько потоков, далее сравниваются предполагаемые и фактические значения `balance`. 

Описание endpoints:
`/accounts/create-acc` - создать пользователя. Принимает на вход Json с полями ownerName и amount.
`/accounts/put-amount` - положить деньги на счет. Принимает на вход Json с полями accountMain (id счета, куда положить) и amount.
`/accounts/withdraw-amount` - снять деньги со счета. Принимает на вход Json с полями accountMain (id счета, куда положить) и amount.
`/accounts/transfer-amount` - перевести деньги со счета на счет. Принимает на вход Json с полями accountMain (откуда переводить), accountExternal (куда переводить) и amount.

Запуск приложения с тестами: ``
