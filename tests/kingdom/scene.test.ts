import {parseKingmaker, parseSceneTiles, RealmTileData, StolenLandsData} from '../../src/kingdom/scene';

describe('parse kingmaker module data', () => {
    it('should compute kingmaker module state', () => {
        const kingmaker: KingmakerState = {
            hexes: {
                100: {claimed: true, commodity: 'food'},
                101: {claimed: true, commodity: 'lumber'},
                102: {claimed: true, commodity: 'lumber', camp: 'quarry'},
                103: {claimed: true, camp: 'lumber'},
                104: {claimed: true, commodity: 'lumber', camp: 'lumber'},
                105: {claimed: true, commodity: 'lumber', camp: 'lumber'},
                106: {claimed: true, commodity: 'lumber', camp: 'lumber'},
                107: {claimed: true, commodity: 'stone', camp: 'quarry'},
                108: {claimed: true, commodity: 'stone', camp: 'quarry'},
                109: {claimed: true, commodity: 'ore', camp: 'mine'},
                110: {claimed: true, camp: 'lumber'},
                111: {claimed: true, camp: 'mine'},
                112: {claimed: true, features: [{type: 'farmland'}]},
                113: {claimed: false, camp: 'mine'},
                114: {claimed: true, camp: 'mine', commodity: 'luxuries'},
                115: {claimed: true, commodity: 'luxuries'},
            },
        };
        const result = parseKingmaker(kingmaker);
        const expected: StolenLandsData = {
            size: 15,
            workSites: {
                luxurySources: {quantity: 1, resources: 0},
                mines: {quantity: 2, resources: 1},
                farmlands: {quantity: 1, resources: 0},
                quarries: {quantity: 3, resources: 2},
                lumberCamps: {quantity: 5, resources: 3},
            },
        };
        expect(result).toEqual(expected);
    });

    it('should parse claimed scene lumber tiles', () => {
        const data: RealmTileData[] = [{
            type: 'claimed',
            xStart: 0,
            xEnd: 10,
            yStart: 0,
            yEnd: 10,
        }, {
            type: 'lumberCamp',
            xStart: -1,
            xEnd: 11,
            yStart: -1,
            yEnd: 11,
        }, {
            type: 'lumberCamp',
            xStart: 0,
            xEnd: 1,
            yStart: 0,
            yEnd: 1,
        }, {
            type: 'lumberCamp',
            xStart: 12,
            xEnd: 13,
            yStart: 11,
            yEnd: 12,
        }, {
            type: 'lumber',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }];
        const result = parseSceneTiles(data, 1);
        const expected = {
            size: 1,
            worksites: {
                luxurySources: {quantity: 0, resources: 0},
                mines: {quantity: 0, resources: 0},
                farmlands: {quantity: 0, resources: 0},
                quarries: {quantity: 0, resources: 0},
                lumberCamps: {quantity: 2, resources: 1},
            },
        };
        expect(result).toEqual(expected);
    });

    it('should parse claimed scene quarry tiles', () => {
        const data: RealmTileData[] = [{
            type: 'claimed',
            xStart: 0,
            xEnd: 10,
            yStart: 0,
            yEnd: 10,
        }, {
            type: 'quarry',
            xStart: -1,
            xEnd: 11,
            yStart: -1,
            yEnd: 11,
        }, {
            type: 'quarry',
            xStart: 0,
            xEnd: 1,
            yStart: 0,
            yEnd: 1,
        }, {
            type: 'quarry',
            xStart: 12,
            xEnd: 13,
            yStart: 11,
            yEnd: 12,
        }, {
            type: 'stone',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }];
        const result = parseSceneTiles(data, 1);
        const expected = {
            size: 1,
            worksites: {
                luxurySources: {quantity: 0, resources: 0},
                mines: {quantity: 0, resources: 0},
                farmlands: {quantity: 0, resources: 0},
                quarries: {quantity: 2, resources: 1},
                lumberCamps: {quantity: 0, resources: 0},
            },
        };
        expect(result).toEqual(expected);
    });

    it('should parse claimed scene mine tiles', () => {
        const data: RealmTileData[] = [{
            type: 'claimed',
            xStart: 0,
            xEnd: 10,
            yStart: 0,
            yEnd: 10,
        }, {
            type: 'mine',
            xStart: -1,
            xEnd: 11,
            yStart: -1,
            yEnd: 11,
        }, {
            type: 'mine',
            xStart: 0,
            xEnd: 1,
            yStart: 0,
            yEnd: 1,
        }, {
            type: 'mine',
            xStart: 12,
            xEnd: 13,
            yStart: 11,
            yEnd: 12,
        }, {
            type: 'ore',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }, {
            type: 'stone',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }];
        const result = parseSceneTiles(data, 1);
        const expected = {
            size: 1,
            worksites: {
                luxurySources: {quantity: 0, resources: 0},
                mines: {quantity: 2, resources: 1},
                farmlands: {quantity: 0, resources: 0},
                quarries: {quantity: 0, resources: 0},
                lumberCamps: {quantity: 0, resources: 0},
            },
        };
        expect(result).toEqual(expected);
    });

    it('should parse claimed scene farmland tiles', () => {
        const data: RealmTileData[] = [{
            type: 'claimed',
            xStart: 0,
            xEnd: 10,
            yStart: 0,
            yEnd: 10,
        }, {
            type: 'farmland',
            xStart: -1,
            xEnd: 11,
            yStart: -1,
            yEnd: 11,
        }, {
            type: 'farmland',
            xStart: 0,
            xEnd: 1,
            yStart: 0,
            yEnd: 1,
        }, {
            type: 'farmland',
            xStart: 12,
            xEnd: 13,
            yStart: 11,
            yEnd: 12,
        }, {
            type: 'ore',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }, {
            type: 'stone',
            xStart: 5,
            xEnd: 11,
            yStart: 5,
            yEnd: 11,
        }];
        const result = parseSceneTiles(data, 1);
        const expected = {
            size: 1,
            worksites: {
                luxurySources: {quantity: 0, resources: 0},
                mines: {quantity: 0, resources: 0},
                farmlands: {quantity: 2, resources: 0},
                quarries: {quantity: 0, resources: 0},
                lumberCamps: {quantity: 0, resources: 0},
            },
        };
        expect(result).toEqual(expected);
    });
});