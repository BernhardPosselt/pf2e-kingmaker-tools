export enum DegreeOfSuccess {
    CRITICAL_FAILURE,
    FAILURE,
    SUCCESS,
    CRITICAL_SUCCESS
}

export const allDegreesOfSuccesses = ['criticalSuccess', 'success', 'failure', 'criticalFailure'] as const;
export type StringDegreeOfSuccess = typeof allDegreesOfSuccesses[number];

