# Module ER diagram description. Employee

Глобальный модуль employee отвечает за backoffice/call-center сценарии, которые выполняются от имени клиента или партнера. Он не становится владельцем заказов, корзины, профиля или претензий, а хранит employee-specific контекст: support session, operator order reference, support action и audit event.

Модуль зависит от order, cart, profile и partner-related модулей через сервисные контракты или future integration adapters. Собственные runtime-классы размещаются только в `impl` role subpackages. Liquibase changeset хранится в XML под changelog employee и не смешивается с order changelog.