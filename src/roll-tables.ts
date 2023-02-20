export interface RollTableResult {
    table: RollTable;
    draw: RollTableDraw;
}

export async function findRollTableUuidWithFallback(
    game: Game,
    tableName: string,
    compendiumName = 'pf2e-kingmaker-tools.kingmaker-tools-rolltables',
): Promise<string | undefined> {
    const worldUuid = findWorldTableUuid(game, tableName);
    const compendiumUuid = await findCompendiumTableUuid(game, tableName, compendiumName);
    // first look through world tables, then fall back to compendium tables
    console.log(`Looking up ${tableName} in ${compendiumName}, world: ${worldUuid}, compendium: ${compendiumUuid}`);
    return worldUuid ?? compendiumUuid;
}

export function getWorldTables(game: Game): RollTable[] {
    return game?.tables?.map(t => t) ?? [];
}

export function getWorldTableUuidMappings(game: Game): Record<string, string> {
    const tables = getWorldTables(game);
    return Object.fromEntries(tables.map((t: RollTable) => [`RollTable.${t.id!}`, t.name!]));
}

export function findWorldTableUuid(game: Game, tableName: string): string | undefined {
    return Object.entries(getWorldTableUuidMappings(game))
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        .filter(([_, value]) => value === tableName)
        .map(([key]) => key)[0];
}

export async function findCompendiumTableUuid(
    game: Game,
    tableName: string,
    compendiumName = 'pf2e-kingmaker-tools.kingmaker-tools-rolltables',
): Promise<string | undefined> {
    const compendiumUuids = await buildUuids(game, compendiumName);
    return compendiumUuids[tableName];
}

export async function rollRollTable(
    game: Game,
    tableUuid: string,
    rollOptions: Partial<RollTable.DrawOptions>,
): Promise<RollTableResult> {
    const table = await fromUuid(tableUuid);
    if (table && table instanceof RollTable) {
        return {
            table,
            draw: await table.draw(rollOptions),
        };
    } else {
        throw new Error(`Could not find table with uuid ${tableUuid}`);
    }
}

export async function buildUuids(
    game: Game,
    compendiumName = 'pf2e-kingmaker-tools.kingmaker-tools-rolltables',
): Promise<Record<string, string>> {
    const compendium = await game.packs.get(compendiumName, {strict: true});
    const documents = await compendium.getDocuments({});
    return Object.fromEntries(documents.map(document => [document.name, `Compendium.${compendiumName}.${document.id}`]));
}
