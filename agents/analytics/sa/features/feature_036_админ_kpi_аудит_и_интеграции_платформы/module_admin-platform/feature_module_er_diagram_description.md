# ER description feature #36

Модуль `admin-platform` владеет read-model для KPI snapshots, integration statuses, report export jobs, alerts и audit events. Domain package предназначен для JPA entities и repositories в будущей persistence-реализации; текущий инкремент фиксирует contract-first модель и dedicated Liquibase XML changeset.