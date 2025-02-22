create table model
(
    id                                 varchar(255) not null,
    created_by                         varchar(255),
    creation_date                      timestamp,
    last_modified_by                   varchar(255),
    last_modified_date                 timestamp,
    name                               varchar(255),
    template                           varchar(255),
    type                               varchar(255),
    latest_version_version             varchar(255),
    latest_version_versioned_entity_id varchar(255),
    category                           varchar(255),
    scope                              int,
    primary key (id)
);
create table model_versions
(
    model_id                     varchar(255) not null,
    versions_version             varchar(255) not null,
    versions_versioned_entity_id varchar(255) not null
);
create table model_version
(
    version             varchar(255) not null,
    created_by          varchar(255),
    creation_date       timestamp,
    last_modified_by    varchar(255),
    last_modified_date  timestamp,
    content             blob,
    content_type        varchar(255),
    extensions          text,
    versioned_entity_id varchar(255) not null,
    primary key (version, versioned_entity_id)
);
create table project
(
    id                 varchar(255) not null,
    created_by         varchar(255),
    creation_date      timestamp,
    last_modified_by   varchar(255),
    last_modified_date timestamp,
    description        varchar(255),
    name               varchar(255),
    disp_name          varchar(255),
    version            varchar(255),
    primary key (id)
);
create table project_models
(
    project_id varchar(255) not null,
    models_id  varchar(255) not null,
    primary key (project_id, models_id)
);
alter table model_versions
    add constraint UK_ei9juhk09r20q4bmvgpjrcrs3 unique (versions_version, versions_versioned_entity_id);
alter table project
    add constraint unique_project_name_createdby unique (name, created_by);
alter table project
    add constraint unique_project_display_name_createdby unique (disp_name, created_by);
alter table model
    add constraint FKqjpgrrtoo1bryor3iymmb03pu foreign key (latest_version_version, latest_version_versioned_entity_id) references model_version;
alter table model_versions
    add constraint FKl24hq09np4hw7g2fwn98jai6b foreign key (versions_version, versions_versioned_entity_id) references model_version;
alter table model_versions
    add constraint FKq32aa8acvlsih8d4h1flpi7g1 foreign key (model_id) references model;
alter table model_version
    add constraint FKsy8h8xspdvbkm0wmi9yw54mio foreign key (versioned_entity_id) references model;
alter table project_models
    add constraint FK63sxj28jbq3gvo0tmfq0vcb4r foreign key (models_id) references model;
alter table project_models
    add constraint FKn22a4qub8h6etnqgxv9qlu31m foreign key (project_id) references project;
