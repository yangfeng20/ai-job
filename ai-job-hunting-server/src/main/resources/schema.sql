CREATE DATABASE IF NOT EXISTS ai_job
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;


create table ai_job.msg_session
(
    id           bigint auto_increment comment '消息会话id'
        primary key,
    msg_context  text charset utf8mb4 null comment '消息上下文',
    ai_type      int                  null comment 'ai类型',
    status       int                  null comment '状态',
    user_id      int                  null comment '用户id',
    session_key  varchar(64)          null comment '会话key(用于job唯一键)',
    is_active    bit default b'0'     null comment '是否活跃',
    created_id   bigint               null comment '创建人',
    created_date datetime             null comment '创建时间',
    updated_id   bigint               null comment '更新人',
    updated_date datetime             null comment '更新时间'
)
    comment '消息会话表';

create table ai_job.`order`
(
    id           bigint                        not null comment '订单id'
        primary key,
    status       int                           null comment '状态',
    user_id      int                           null comment '用户id',
    type         int                           null comment '类型(及产品)',
    is_active    bit            default b'0'   null comment '是否活跃',
    created_id   bigint                        null comment '创建人',
    created_date datetime                      null comment '创建时间',
    updated_id   bigint                        null comment '更新人',
    updated_date datetime                      null comment '更新时间',
    price        decimal(10, 4) default 0.0000 null comment '价格'
);

create table ai_job.user_ai_config
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    user_id          bigint               not null comment '用户ID',
    provider         int     default 0    not null comment 'API提供商(0-自定义 1-DeepSeek 2-火山引擎 3-硅基流动 4-月之暗面 5-OpenRouter)',
    model_name       varchar(128)         null comment '模型名称',
    api_key          varchar(255)         not null comment 'API密钥',
    base_url         varchar(1024)        null comment '基础URL',
    completions_path varchar(255)         null comment '会话路径',
    timeout          int     default 30   null comment '超时时间(秒)',
    test_passed      tinyint default 0    not null comment '测试是否通过(0-未通过 1-已通过)',
    status           tinyint default 1    not null comment '状态(0-禁用 1-启用)',
    is_active        bit     default b'0' null comment '是否活跃',
    created_id       bigint               null comment '创建人',
    created_date     datetime             null comment '创建时间',
    updated_id       bigint               null comment '更新人',
    updated_date     datetime             null comment '更新时间',
    user_prompt      text                 null comment '用户提示词'
)
    comment '用户AI配置表' charset = utf8mb4;

create index idx_status
    on ai_job.user_ai_config (status);

create index idx_user_id
    on ai_job.user_ai_config (user_id);

create table ai_job.user_info
(
    id               bigint auto_increment
        primary key,
    phone            varchar(11)          null comment '手机号',
    email            varchar(56)          null comment '邮件',
    preference       text charset utf8mb4 null,
    is_active        bit default b'0'     null comment '是否活跃',
    created_id       bigint               null comment '创建人',
    created_date     datetime             null comment '创建时间',
    updated_id       bigint               null comment '更新人',
    updated_date     datetime             null comment '更新时间',
    unique_id        varchar(32)          null comment '平台唯一id;boss平台id',
    ai_seat_status   int                  null comment 'ai坐席状态 null-未使用 1-使用中(开) 0-使用中(关)',
    invite_code      varchar(64)          null comment '用户邀请码',
    bind_invite_code varchar(64)          null comment '绑定的邀请码'
)
    comment '用户信息表';

create table ai_job.user_invites
(
    id                  int auto_increment
        primary key,
    invite_code         varchar(64)      not null comment '邀请码',
    to_inviter_user_id  int              not null comment '邀请人id',
    be_invitee_user_id  int              not null comment '被邀请人id',
    be_invitee_username varchar(64)      null comment '被邀请人名称',
    status              int              not null comment '邀请状态',
    is_active           bit default b'0' null comment '是否活跃',
    created_id          bigint           null comment '创建人',
    created_date        datetime         null comment '创建时间',
    updated_id          bigint           null comment '更新人',
    updated_date        datetime         null comment '更新时间',
    constraint be_invitee_user_id
        unique (be_invitee_user_id)
)
    comment '用户邀请表';

create table ai_job.user_product
(
    id                            bigint auto_increment
        primary key,
    user_id                       bigint           null,
    order_id                      bigint           null,
    product_id                    bigint           null,
    product_type                  varchar(64)      null comment '产品类型（json）',
    period_of_validity_start_time datetime         null comment '有效期开始时间',
    period_of_validity_end_time   datetime         null comment '有效期结束时间',
    is_active                     bit default b'0' null comment '是否活跃',
    created_id                    bigint           null comment '创建人',
    created_date                  datetime         null comment '创建时间',
    updated_id                    bigint           null comment '更新人',
    updated_date                  datetime         null comment '更新时间'
)
    comment '用户商品表';

create table ai_job.user_resume
(
    id             bigint auto_increment
        primary key,
    user_id        bigint               null comment '用户id',
    resume_content text charset utf8mb4 null,
    resume_url     varchar(256)         null comment '保存简历url',
    is_active      bit default b'0'     null comment '是否活跃',
    created_id     bigint               null comment '创建人',
    created_date   datetime             null comment '创建时间',
    updated_id     bigint               null comment '更新人',
    updated_date   datetime             null comment '更新时间',
    oss_file_name  varchar(64)          null comment 'oss唯一对象名',
    preset_problem text charset utf8mb4 null,
    resume_id      varchar(64)          null comment 'boss平台简历id'
)
    comment '用户简历表';

create table ai_job.user_trial
(
    id           bigint auto_increment
        primary key,
    user_id      bigint           null comment '用户id',
    product_type int              null comment '产品类型(产品能力)',
    trial_count  int              null comment '试用次数',
    is_active    bit default b'0' null comment '是否活跃',
    created_id   bigint           null comment '创建人',
    created_date datetime         null comment '创建时间',
    updated_id   bigint           null comment '更新人',
    updated_date datetime         null comment '更新时间',
    `desc`       varchar(64)      null comment '描述'
)
    comment '用户试用表';

