export enum DegreeOfSuccess {
    CRITICAL_FAILURE,
    FAILURE,
    SUCCESS,
    CRITICAL_SUCCESS
}


function adjustByDieNumber(dieNumber: number, degree: DegreeOfSuccess): DegreeOfSuccess {
    if (dieNumber === 1) {
        return Math.max(0, degree - 1);
    } else if (dieNumber === 20) {
        return Math.min(3, degree + 1);
    } else {
        return degree;
    }
}

export function determineDegreeOfSuccess(
    dieNumber: number,
    result: number,
    dc: number
): DegreeOfSuccess {
    if (result <= dc - 10) {
        return adjustByDieNumber(dieNumber, DegreeOfSuccess.CRITICAL_FAILURE);
    } else if (result >= dc + 10) {
        return adjustByDieNumber(dieNumber, DegreeOfSuccess.CRITICAL_SUCCESS);
    } else if (result >= dc) {
        return adjustByDieNumber(dieNumber, DegreeOfSuccess.SUCCESS);
    } else {
        return adjustByDieNumber(dieNumber, DegreeOfSuccess.FAILURE);
    }
}
