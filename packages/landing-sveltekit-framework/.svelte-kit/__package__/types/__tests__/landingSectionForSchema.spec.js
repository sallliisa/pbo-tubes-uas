import { describe, expectTypeOf, it } from 'vitest';
import { defineSectionSchema } from '../index.js';
const contentDefaultSchema = defineSectionSchema({
    code: 'content-default',
    meta: { fields: ['width_preset', 'content_order'] },
    data: {
        content: { type: 'content', order: 1, fields: ['title', 'description'] },
    },
});
const contentGallerySchema = defineSectionSchema({
    code: 'content-gallery',
    meta: { fields: ['width_preset'] },
    data: {
        gallery: { type: 'gallery', order: 1, many: true, fields: ['media', 'title'] },
    },
});
const fieldSets = {
    list: { fields: ['title', 'description'] },
    card: { fields: ['media', 'attachment'] },
};
const heroBannerSchema = defineSectionSchema({
    code: 'hero-banner',
    data: {
        banner: { type: 'gallery', order: 1, many: true, fields: ['media'] },
        projectCategory: { type: 'gallery', order: 3, many: true, fieldSets, fields: ['title', 'description', 'media', 'attachment'] },
    },
});
const dataListSchema = defineSectionSchema({
    code: 'data-list',
    meta: { fields: ['type'] },
    data: {
        childSections: {
            type: 'sectionGroup',
            order: 1,
            many: true,
            schema: {
                data: {
                    gallery: { type: 'gallery', order: 1, fieldSets, fields: ['title', 'description', 'media', 'attachment'] },
                },
            },
        },
    },
});
describe('LandingSectionForSchema typing', () => {
    it('infers slot and meta keys', () => {
        expectTypeOf().toMatchTypeOf();
        expectTypeOf().toMatchTypeOf();
        expectTypeOf().toMatchTypeOf();
    });
    it('infers many slots and nested schema data', () => {
        expectTypeOf().toMatchTypeOf();
        expectTypeOf().toMatchTypeOf();
        void null;
        void null;
    });
});
