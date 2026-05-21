import type { Component } from 'svelte';
export type AnyRecord = Record<string, any>;
export type WithElementRef<T, U extends HTMLElement = HTMLElement> = T & {
    ref?: U | null;
};
export type LandingSection = AnyRecord & {
    id: string;
    visible?: boolean;
    section_type_code?: string | null;
    data?: unknown;
    meta?: unknown;
};
export type SectionSchemaSlotType = 'content' | 'gallery' | 'section' | 'sectionGroup';
export type SectionEditorComponentToken = string;
export type SectionSchemaEditorInputConfig = Record<string, any>;
export type SectionSchemaSlotEditorContext = {
    slot: {
        key: string;
        type: SectionSchemaSlotType;
        order: number;
        many: boolean;
    };
    sectionData?: Record<string, unknown> | null;
    parentSectionData?: Record<string, unknown> | null;
    rootSectionData?: Record<string, unknown> | null;
};
export type SectionSchemaSlotEditor = {
    label?: string;
    fieldAliases?: Record<string, string>;
    inputConfig?: SectionSchemaEditorInputConfig;
    fieldsDictionary?: Record<string, unknown>;
    fieldsParse?: Record<string, unknown>;
    fieldsProxy?: Record<string, unknown>;
    fieldsType?: Record<string, unknown>;
    fieldsUnit?: Record<string, unknown>;
    defaultValues?: Record<string, unknown>;
    onDragChange?: (event: any) => void;
    componentToken?: SectionEditorComponentToken;
    resolveConfig?: (ctx: SectionSchemaSlotEditorContext) => SectionSchemaSlotEditorResolvedConfig;
};
export type SectionSchemaSlotEditorResolvedConfig = Omit<SectionSchemaSlotEditor, 'resolveConfig'> & {
    fieldSet?: string;
};
export type SectionSchemaMetaEditor = {
    inputConfig?: SectionSchemaEditorInputConfig;
    fieldsAlias?: Record<string, string>;
    getInitialData?: () => Promise<Record<string, unknown>>;
};
export type NestedSectionSchema = {
    info?: {
        name?: string;
        description?: string;
    };
    editor?: {
        group?: string;
    };
    meta?: SectionSchemaMeta;
    data: Record<string, SectionSchemaSlot>;
};
export type SectionSchemaSlot = {
    type: SectionSchemaSlotType;
    order: number;
    many?: boolean;
    fields?: readonly string[];
    fieldSets?: Record<string, {
        fields: readonly string[];
    }>;
    schema?: NestedSectionSchema;
    editor?: SectionSchemaSlotEditor;
};
export type SectionSchemaMeta = {
    fields?: readonly string[];
    defaultValues?: Record<string, unknown>;
    editor?: SectionSchemaMetaEditor;
};
export type SectionSchema = {
    code: string;
    info?: {
        name?: string;
        description?: string;
    };
    editor?: {
        group?: string;
    };
    meta?: SectionSchemaMeta;
    data: Record<string, SectionSchemaSlot>;
};
export type SectionSchemaRegistry = Record<string, SectionSchema>;
export declare function defineSectionSchema<const TSchema extends SectionSchema>(schema: TSchema): TSchema;
export type SectionMetaField<TSchema extends SectionSchema> = TSchema['meta'] extends {
    fields?: readonly (infer TField)[];
} ? TField extends string ? TField : never : never;
export type SectionMetaValues<TSchema extends SectionSchema> = Partial<Record<SectionMetaField<TSchema>, any>>;
type SlotField<TSlot extends SectionSchemaSlot> = TSlot extends {
    fields?: readonly (infer TField)[];
} ? TField extends string ? TField : never : never;
type SlotFieldSetField<TSlot extends SectionSchemaSlot> = TSlot extends {
    fieldSets?: Record<string, {
        fields: readonly (infer TField)[];
    }>;
} ? TField extends string ? TField : never : never;
type SlotValueForSchema<TSlot extends SectionSchemaSlot> = TSlot['schema'] extends NestedSectionSchema ? LandingSectionForNestedSchema<TSlot['schema']> : Partial<Record<SlotField<TSlot> | SlotFieldSetField<TSlot>, any>>;
type SlotDataValue<TSlot extends SectionSchemaSlot> = TSlot['many'] extends true ? Array<SlotValueForSchema<TSlot>> : SlotValueForSchema<TSlot> | null;
type SectionDataValues<TSchema extends {
    data: Record<string, SectionSchemaSlot>;
}> = {
    [TSlotKey in keyof TSchema['data']]: SlotDataValue<TSchema['data'][TSlotKey]>;
};
type LandingSectionForNestedSchema<TSchema extends NestedSectionSchema> = Omit<LandingSection, 'meta' | 'data'> & {
    meta: (TSchema['meta'] extends SectionSchemaMeta ? Partial<Record<TSchema['meta']['fields'] extends readonly (infer TField)[] ? TField extends string ? TField : never : never, any>> : Record<string, unknown>) & Record<string, any>;
    data: SectionDataValues<TSchema> & Record<string, any>;
};
export type LandingSectionForSchema<TSchema extends SectionSchema> = Omit<LandingSection, 'meta' | 'data'> & {
    meta: SectionMetaValues<TSchema> & Record<string, any>;
    data: SectionDataValues<TSchema> & Record<string, any>;
};
export type SectionDataLoader<TSection extends LandingSection = LandingSection> = (section: TSection, context?: AnyRecord) => Promise<unknown>;
export type SectionLoaderRegistry<TSection extends LandingSection = LandingSection> = Record<string, SectionDataLoader<TSection>>;
export type SectionComponentModule = {
    default: Component<any>;
};
export type SectionComponentRegistry = Record<string, () => Promise<SectionComponentModule>>;
export type CrudOperation = 'list' | 'detail' | 'create' | 'update' | 'delete' | 'reorder' | 'verify';
export type PaginationOptions = {
    page?: number;
    limit?: number;
};
export type PaginationLoaderResult<T> = {
    data: T[];
    total: number;
};
export type ReorderEntriesOptions = {
    prisma: any;
    model: string;
    id: string | number;
    from: number;
    to: number;
    axis: string[];
    orderField?: string;
};
export type ReorderPayload = {
    id: string | number;
    from: number;
    to: number;
};
export type PaginatedResponse<T> = {
    data: T[];
    meta: {
        totalRecords: number;
        totalPages: number;
        currentPage: number;
        limit: number;
    };
};
export type FrameworkSuccessResponse<TData = unknown, TMeta = Record<string, unknown>> = {
    ok: true;
    data: TData;
    meta?: TMeta;
};
export type FrameworkErrorResponse = {
    ok: false;
    error: {
        message: string;
        code?: string;
        details?: unknown;
    };
};
export type FrameworkErrorLike = Error & {
    status?: number;
    statusCode?: number;
    code?: string;
    details?: unknown;
};
export type OperationLifecycle = {
    pre?: (...args: any[]) => any | Promise<any>;
    main?: (...args: any[]) => any | Promise<any>;
    post?: (...args: any[]) => any | Promise<any>;
};
export type FieldValidator = (value: any, body: AnyRecord) => boolean | string | Promise<boolean | string>;
export type FieldValidationRule = {
    validator: FieldValidator;
    message?: string;
};
export type FieldValidationConfig = Record<string, FieldValidationRule[]>;
export type RelationSelectConfig = {
    fields?: string[];
    fieldsForeign?: FieldsForeignConfig;
};
export type FieldsForeignConfig = Record<string, RelationSelectConfig>;
export type BaseOperationConfig<T = AnyRecord> = {
    allow?: boolean;
    permission?: string;
    authorize?: (event: any, input: AnyRecord) => void | Promise<void>;
    fields?: string[];
    fieldsForeign?: FieldsForeignConfig;
    by?: string[];
    where?: (event: any) => AnyRecord | Promise<AnyRecord> | undefined;
    validation?: FieldValidationConfig;
    lifecycle?: OperationLifecycle;
};
export type ModelConfig<T = AnyRecord> = BaseOperationConfig<T> & {
    types?: AnyRecord;
    view?: {
        fieldsForeign?: FieldsForeignConfig;
        customFields?: Array<{
            name: string;
            generator: (data: AnyRecord) => unknown;
        }>;
    };
    transaction?: {
        lifecycle?: OperationLifecycle;
    };
    list?: BaseOperationConfig<T> & {
        filterableBy?: string[];
        searchableBy?: string[];
        orderBy?: AnyRecord;
        customFields?: Array<{
            name: string;
            generator: (data: AnyRecord) => unknown;
        }>;
    };
    detail?: BaseOperationConfig<T> & {
        customFields?: Array<{
            name: string;
            generator: (data: AnyRecord) => unknown;
        }>;
    };
    create?: BaseOperationConfig<T>;
    update?: BaseOperationConfig<T>;
    delete?: BaseOperationConfig<T>;
    reorder?: BaseOperationConfig<T> & {
        axis: string[];
    };
    verify?: Omit<BaseOperationConfig<T>, 'by'> & {
        by?: string;
        stateField?: string;
        initialState?: string;
        states?: string[];
        transitions?: Record<string, {
            from: string | string[];
            to: string;
        }>;
    };
};
export type ModelConfigRegistry = Record<string, ModelConfig | (() => Promise<any>)>;
export type LandingFrameworkConfig = {
    prisma: any;
    getLocale: () => string;
    modelConfigs?: ModelConfigRegistry;
    sectionSchemas?: SectionSchemaRegistry;
    auth?: {
        hydrateRequestAuth?: (event: any) => Promise<void>;
        requireAuthenticatedUser?: (locals: any) => unknown;
        isProtectedRoute?: (pathname: string) => boolean;
    };
    storage?: {
        root: string;
        publicBaseUrl?: string;
    };
};
export {};
//# sourceMappingURL=index.d.ts.map