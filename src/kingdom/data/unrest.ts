export function calculateUnrestPenalty(unrest: number): number {
    if (unrest < 1) {
        return 0;
    } else if (unrest < 5) {
        return 1;
    } else if (unrest < 10) {
        return 2;
    } else if (unrest < 15) {
        return 3;
    } else {
        return 4;
    }
}
