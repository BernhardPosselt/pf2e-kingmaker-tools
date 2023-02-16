import {getControlDC} from '../../src/kingdom/data/kingdom';

describe('control DC', () => {
    test('calculate DC', () => {
        expect(getControlDC(1, 1)).toBe(14);
        expect(getControlDC(2, 1)).toBe(15);
        expect(getControlDC(3, 1)).toBe(16);
        expect(getControlDC(4, 1)).toBe(18);
        expect(getControlDC(5, 1)).toBe(20);
        expect(getControlDC(6, 1)).toBe(22);
        expect(getControlDC(7, 1)).toBe(23);
        expect(getControlDC(8, 1)).toBe(24);
        expect(getControlDC(9, 1)).toBe(26);
        expect(getControlDC(10, 1)).toBe(27);
        expect(getControlDC(11, 1)).toBe(28);
        expect(getControlDC(12, 1)).toBe(30);
        expect(getControlDC(13, 1)).toBe(31);
        expect(getControlDC(14, 1)).toBe(32);
        expect(getControlDC(15, 1)).toBe(34);
        expect(getControlDC(16, 1)).toBe(35);
        expect(getControlDC(17, 1)).toBe(36);
        expect(getControlDC(18, 1)).toBe(38);
        expect(getControlDC(19, 1)).toBe(39);
        expect(getControlDC(20, 1)).toBe(40);
    });
    test('calculate size dc', () => {
        expect(getControlDC(1, 9)).toBe(14);
        expect(getControlDC(1, 24)).toBe(15);
        expect(getControlDC(1, 49)).toBe(16);
        expect(getControlDC(1, 99)).toBe(17);
        expect(getControlDC(1, 100)).toBe(18);
    });
});
