import {DegreeOfSuccess, determineDegreeOfSuccess} from '../src/degree-of-success';

describe('degree of success', () => {
    test('normal dc tests', () => {
        expect(determineDegreeOfSuccess(10, 10, 20)).toBe(DegreeOfSuccess.CRITICAL_FAILURE);
        expect(determineDegreeOfSuccess(10, 10, 11)).toBe(DegreeOfSuccess.FAILURE);
        expect(determineDegreeOfSuccess(10, 10, 10)).toBe(DegreeOfSuccess.SUCCESS);
        expect(determineDegreeOfSuccess(10, 10, 0)).toBe(DegreeOfSuccess.CRITICAL_SUCCESS);
    });

    test('rolling 1 dc tests', () => {
        expect(determineDegreeOfSuccess(1, 10, 20)).toBe(DegreeOfSuccess.CRITICAL_FAILURE);
        expect(determineDegreeOfSuccess(1, 10, 11)).toBe(DegreeOfSuccess.CRITICAL_FAILURE);
        expect(determineDegreeOfSuccess(1, 10, 10)).toBe(DegreeOfSuccess.FAILURE);
        expect(determineDegreeOfSuccess(1, 10, 0)).toBe(DegreeOfSuccess.SUCCESS);
    });

    test('rolling 20 dc tests', () => {
        expect(determineDegreeOfSuccess(20, 10, 20)).toBe(DegreeOfSuccess.FAILURE);
        expect(determineDegreeOfSuccess(20, 10, 11)).toBe(DegreeOfSuccess.SUCCESS);
        expect(determineDegreeOfSuccess(20, 10, 10)).toBe(DegreeOfSuccess.CRITICAL_SUCCESS);
        expect(determineDegreeOfSuccess(20, 10, 0)).toBe(DegreeOfSuccess.CRITICAL_SUCCESS);
    });
});
