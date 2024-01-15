import {Structure} from '../data/structures';
import {capitalize} from '../../utils';

export interface Costs {
    rp: number;
    ore: number;
    stone: number;
    lumber: number;
    luxuries: number;
}

function calculateCosts(structure: Structure, mode: 'half' | 'full'): Costs {
    const costs: Costs = {
        rp: structure.construction?.rp ?? 0,
        ore: structure.construction?.ore ?? 0,
        lumber: structure.construction?.lumber ?? 0,
        luxuries: structure.construction?.luxuries ?? 0,
        stone: structure.construction?.stone ?? 0,
    };
    if (mode === 'full') {
        return costs;
    } else {
        return {
            rp: costs.rp,
            ore: Math.ceil(costs.ore / 2),
            lumber: Math.ceil(costs.lumber / 2),
            luxuries: Math.ceil(costs.luxuries / 2),
            stone: Math.ceil(costs.stone / 2),
        };
    }
}

export function formatCosts(costs: Costs): string {
    return Object.entries(costs)
        .filter(([, value]) => value > 0)
        .map(([key, value]) => {
            return `${capitalize(key)}: ${value}`;
        })
        .join(', ');
}

export function payDialog(
    structure: Structure,
    pay: (costs: Costs) => void,
): void {
    const half = calculateCosts(structure, 'half');
    const full = calculateCosts(structure, 'full');
    new Dialog({
        title: `Consume Commodities: ${structure.name}`,
        content: `
        <p>Costs</p>
        <ul>
            <li><b>Half</b>: ${formatCosts(half)}</li>
            <li><b>Full</b>: ${formatCosts(full)}</li>
        </ul>
        `,
        buttons: {
            half: {
                label: 'Pay Half',
                callback: async (): Promise<void> => {
                    pay(half);
                },
            },
            full: {
                label: 'Pay Full',
                callback: async (): Promise<void> => {
                    pay(full);
                },
            },
        },
        default: 'importSheet',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}