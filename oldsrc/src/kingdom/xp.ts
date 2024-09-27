export function calculateHexXP(
    {
        hexes,
        kingdomSize,
        useVK,
        xpPerClaimedHex,
    }: {
        hexes: number,
        xpPerClaimedHex: number,
        kingdomSize: number,
        useVK: boolean
    },
): number {
    if (useVK) {
        if (kingdomSize < 10) {
            return hexes * 100;
        } else if (kingdomSize < 25) {
            return hexes * 50;
        } else if (kingdomSize < 50) {
            return hexes * 25;
        } else if (kingdomSize < 100) {
            return hexes * 10;
        } else {
            return hexes * 5;
        }
    } else {
        return hexes * xpPerClaimedHex;
    }
}

export function calculateRpXP(
    {
        rp,
        kingdomLevel,
        rpToXpConversionRate,
        rpToXpConversionLimit,
        useVK,
    }: {
        rp: number,
        kingdomLevel: number,
        rpToXpConversionRate: number,
        rpToXpConversionLimit: number,
        useVK: boolean
    },
): number {
    let xp = 0;
    if (useVK) {
        if (kingdomLevel < 5) {
            xp = rp * 10;
        } else if (kingdomLevel < 9) {
            xp = rp * 7;
        } else if (kingdomLevel < 13) {
            xp = rp * 5;
        } else if (kingdomLevel < 17) {
            xp = rp * 2;
        }
    } else {
        xp = rp * rpToXpConversionRate;
    }
    return Math.min(rpToXpConversionLimit, xp);
}

export function calculateEventXP(modifier: number): number {
    const xp: Record<string, number> = {
        '-4': 10,
        '-3': 15,
        '-2': 20,
        '-1': 30,
        '0': 40,
        '1': 60,
        '2': 80,
        '3': 120,
        '4': 160,
    };
    const key = `${modifier}`;
    if (key in xp) {
        return xp[key];
    } else {
        return 0;
    }
}
