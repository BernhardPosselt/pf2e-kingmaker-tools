import {getControlDC} from '../../src/kingdom/data/kingdom';

describe('control DC', () => {
    test('calculate DC', () => {
        expect(getControlDC(1, 1, false)).toBe(14);
        expect(getControlDC(2, 1, false)).toBe(15);
        expect(getControlDC(3, 1, false)).toBe(16);
        expect(getControlDC(4, 1, false)).toBe(18);
        expect(getControlDC(5, 1, false)).toBe(20);
        expect(getControlDC(6, 1, false)).toBe(22);
        expect(getControlDC(7, 1, false)).toBe(23);
        expect(getControlDC(8, 1, false)).toBe(24);
        expect(getControlDC(9, 1, false)).toBe(26);
        expect(getControlDC(10, 1, false)).toBe(27);
        expect(getControlDC(11, 1, false)).toBe(28);
        expect(getControlDC(12, 1, false)).toBe(30);
        expect(getControlDC(13, 1, false)).toBe(31);
        expect(getControlDC(14, 1, false)).toBe(32);
        expect(getControlDC(15, 1, false)).toBe(34);
        expect(getControlDC(16, 1, false)).toBe(35);
        expect(getControlDC(17, 1, false)).toBe(36);
        expect(getControlDC(18, 1, false)).toBe(38);
        expect(getControlDC(19, 1, false)).toBe(39);
        expect(getControlDC(20, 1, false)).toBe(40);
        expect(getControlDC(20, 1, true)).toBe(42);
    });
    test('calculate size dc', () => {
        expect(getControlDC(1, 9, false)).toBe(14);
        expect(getControlDC(1, 24, false)).toBe(15);
        expect(getControlDC(1, 49, false)).toBe(16);
        expect(getControlDC(1, 99, false)).toBe(17);
        expect(getControlDC(1, 100, false)).toBe(18);
    });
});
