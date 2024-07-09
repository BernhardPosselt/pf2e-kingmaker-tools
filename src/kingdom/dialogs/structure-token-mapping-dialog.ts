import {isStructureActor} from '../scene';
import {createUUIDLink, groupBy, isBlank, parseRadio, parseTextInput} from '../../utils';

const officialMappings: Mapping[] = [{
    'name': 'Academy',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/academy.webp',
}, {
    'name': 'Alchemy Laboratory',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/alchemy-laboratory.webp',
}, {
    'name': 'Arcanist\'s Tower',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/arcanists-tower.webp',
}, {'name': 'Arena', 'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/arena.webp'}, {
    'name': 'Bank',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/bank.webp',
}, {
    'name': 'Bank (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/bank.webp',
}, {
    'name': 'Barracks',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/barracks.webp',
}, {
    'name': 'Brewery',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/brewery.webp',
}, {
    'name': 'Castle',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/castle.webp',
}, {
    'name': 'Castle (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/castle.webp',
}, {
    'name': 'Cathedral',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/cathedral.webp',
}, {
    'name': 'Cemetary',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/cemetery.webp',
}, {
    'name': 'Construction Yard',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/construction-yard.webp',
}, {
    'name': 'Construction Yard (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/construction-yard.webp',
}, {'name': 'Dump', 'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/dump.webp'}, {
    'name': 'Embassy',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/embassy.webp',
}, {
    'name': 'Festival Hall',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/festival-hall.webp',
}, {
    'name': 'Festival Hall (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/festival-hall.webp',
}, {
    'name': 'Foundry',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/foundry.webp',
}, {
    'name': 'Garrison',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/garrison.webp',
}, {
    'name': 'Garrison (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/garrison.webp',
}, {
    'name': 'General Store',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/general-store.webp',
}, {
    'name': 'Granary',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/granary.webp',
}, {
    'name': 'Granary (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/granary.webp',
}, {
    'name': 'Guildhall',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/guildhall.webp',
}, {
    'name': 'Herbalist',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/herbalist.webp',
}, {
    'name': 'Hospital',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/hospital.webp',
}, {
    'name': 'Houses',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/houses.webp',
}, {
    'name': 'Illicit Market',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/illicit-market.webp',
}, {'name': 'Inn', 'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/inn.webp'}, {
    'name': 'Inn (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/inn.webp',
}, {'name': 'Jail', 'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/jail.webp'}, {
    'name': 'Keep',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/keep.webp',
}, {
    'name': 'Library',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/library.webp',
}, {
    'name': 'Library (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/library.webp',
}, {
    'name': 'Lumberyard',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/lumberyard.webp',
}, {
    'name': 'Luxury Store',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/luxury-store.webp',
}, {
    'name': 'Magic Shop',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/magic-shop.webp',
}, {
    'name': 'Magic Shop (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/magic-shop.webp',
}, {
    'name': 'Mansion',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/mansion.webp',
}, {
    'name': 'Marketplace',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/marketplace.webp',
}, {
    'name': 'Menagerie',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/menagerie.webp',
}, {
    'name': 'Military Academy',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/military-academy.webp',
}, {'name': 'Mill', 'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/mill.webp'}, {
    'name': 'Mint',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/mint.webp',
}, {
    'name': 'Monument',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/monument.webp',
}, {
    'name': 'Monument (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/monument.webp',
}, {
    'name': 'Museum',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/museum.webp',
}, {
    'name': 'Noble Villa',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/noble-villa.webp',
}, {
    'name': 'Occult Shop',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/occult-shop.webp',
}, {
    'name': 'Occult Shop (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/occult-shop.webp',
}, {
    'name': 'Opera House',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/opera-house.webp',
}, {
    'name': 'Orphanage',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/orphanage.webp',
}, {
    'name': 'Palace',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/palace.webp',
}, {
    'name': 'Palace (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/palace.webp',
}, {
    'name': 'Park',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/park.webp',
}, {
    'name': 'Pier',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/pier.webp',
}, {
    'name': 'Rubble',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/rubble.webp',
}, {
    'name': 'Sacred Grove',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/sacred-grove.webp',
}, {
    'name': 'Secure Warehouse',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/secure-warehouse.webp',
}, {
    'name': 'Shrine',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/shrine.webp',
}, {
    'name': 'Smithy',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/smithy.webp',
}, {
    'name': 'Smithy (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/smithy.webp',
}, {
    'name': 'Specialized Artisan',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/special-artisan.webp',
}, {
    'name': 'Stable',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/stable.webp',
}, {
    'name': 'Stockyard',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/stockyard.webp',
}, {
    'name': 'Stonemason',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/stonemason.webp',
}, {
    'name': 'Tannery',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tannery.webp',
}, {
    'name': 'Tavern, Dive',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-dive.webp',
}, {
    'name': 'Tavern, Dive (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-dive.webp',
}, {
    'name': 'Tavern, Luxury',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-luxury.webp',
}, {
    'name': 'Tavern, Luxury (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-luxury.webp',
}, {
    'name': 'Tavern, Popular',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-popular.webp',
}, {
    'name': 'Tavern, Popular (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-popular.webp',
}, {
    'name': 'Tavern, World-Class',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-worldclass.webp',
}, {
    'name': 'Tavern, World-Class (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tavern-worldclass.webp',
}, {
    'name': 'Temple',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/temple.webp',
}, {
    'name': 'Tenement',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/tenement.webp',
}, {
    'name': 'Theater',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/theatre.webp',
}, {
    'name': 'Thieves\' Guild',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/thieves-guild.webp',
}, {
    'name': 'Town Hall',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/town-hall.webp',
}, {
    'name': 'Town Hall (V&K)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/town-hall.webp',
}, {
    'name': 'Trade Shop',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/trade-shop.webp',
}, {
    'name': 'University',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/university.webp',
}, {
    'name': 'Watchtower',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/watchtower.webp',
}, {
    'name': 'Watchtower, Stone',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/watchtower.webp',
}, {
    'name': 'Waterfront (Corner)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/waterfront.webp',
}, {
    'name': 'Waterfront (Side)',
    'img': 'modules/pf2e-kingmaker/assets/maps-settlements/buildings/waterfront-2.webp',
}];

const customMappings: Mapping[] = [{
    'name': 'Academy',
    'img': 'structures/Academy.webp',
}, {
    'name': 'Alchemy Laboratory',
    'img': 'structures/Alchemy%20Laboratory.webp',
}, {
    'name': 'Arcanist\'s Tower',
    'img': 'structures/Arcanist%20Tower.webp',
}, {'name': 'Arena', 'img': 'structures/Arena.webp'}, {
    'name': 'Bank',
    'img': 'structures/Bank.webp',
}, {'name': 'Bank (V&K)', 'img': 'structures/Bank.webp'}, {
    'name': 'Barracks',
    'img': 'structures/Barracks.webp',
}, {'name': 'Brewery', 'img': 'structures/Brewery.webp'},
    {'name': 'Castle (V&K)', 'img': 'structures/Castle.webp'}, {
        'name': 'Cathedral',
        'img': 'structures/Cathedral.webp',
    }, {'name': 'Cemetary', 'img': 'structures/Cemetery.webp'}, {
        'name': 'Construction Yard',
        'img': 'structures/Construction%20Yard.webp',
    }, {
        'name': 'Construction Yard (V&K)',
        'img': 'structures/Construction%20Yard.webp',
    }, {'name': 'Dump', 'img': 'structures/Dump.webp'}, {
        'name': 'Embassy',
        'img': 'structures/Embassy.webp',
    }, {
        'name': 'Festival Hall',
        'img': 'structures/Festival%20Hall.webp',
    }, {
        'name': 'Festival Hall (V&K)',
        'img': 'structures/Festival%20Hall.webp',
    }, {'name': 'Foundry', 'img': 'structures/Foundry.webp'}, {
        'name': 'Garrison',
        'img': 'structures/Garrison.webp',
    }, {'name': 'Garrison (V&K)', 'img': 'structures/Garrison.webp'}, {
        'name': 'General Store',
        'img': 'structures/General%20Store.webp',
    }, {'name': 'Granary', 'img': 'structures/Granary.webp'}, {
        'name': 'Granary (V&K)',
        'img': 'structures/Granary.webp',
    }, {'name': 'Guildhall', 'img': 'structures/Guildhall.webp'}, {
        'name': 'Herbalist',
        'img': 'structures/Herbalist.webp',
    }, {'name': 'Hospital', 'img': 'structures/Hospital.webp'}, {
        'name': 'Houses',
        'img': 'structures/Houses.webp',
    }, {'name': 'Illicit Market', 'img': 'structures/Illicit%20Market.webp'}, {
        'name': 'Inn',
        'img': 'structures/Inn.webp',
    }, {'name': 'Inn (V&K)', 'img': 'structures/Inn.webp'}, {
        'name': 'Jail',
        'img': 'structures/Jail.webp',
    }, {'name': 'Keep', 'img': 'structures/Keep.webp'}, {
        'name': 'Library',
        'img': 'structures/Library.webp',
    }, {'name': 'Library (V&K)', 'img': 'structures/Library.webp'}, {
        'name': 'Lumberyard',
        'img': 'structures/Lumberyard.webp',
    }, {'name': 'Luxury Store', 'img': 'structures/Luxury%20Store.webp'}, {
        'name': 'Magic Shop',
        'img': 'structures/Magic%20Shop.webp',
    }, {
        'name': 'Magic Shop (V&K)',
        'img': 'structures/Magic%20Shop.webp',
    }, {'name': 'Mansion', 'img': 'structures/Mansion.webp'}, {
        'name': 'Marketplace',
        'img': 'structures/Marketplace.webp',
    }, {'name': 'Menagerie', 'img': 'structures/Menagerie.webp'}, {
        'name': 'Military Academy',
        'img': 'structures/Military%20Academy.webp',
    }, {'name': 'Mill', 'img': 'structures/Mill.webp'}, {
        'name': 'Mint',
        'img': 'structures/Mint.webp',
    }, {'name': 'Monument', 'img': 'structures/Monument.webp'}, {
        'name': 'Monument (V&K)',
        'img': 'structures/Monument.webp',
    }, {'name': 'Museum', 'img': 'structures/Museum.webp'}, {
        'name': 'Noble Villa',
        'img': 'structures/Noble%20Villa.webp',
    }, {
        'name': 'Occult Shop',
        'img': 'structures/Occult%20Shop.webp',
    }, {
        'name': 'Occult Shop (V&K)',
        'img': 'structures/Occult%20Shop.webp',
    }, {'name': 'Opera House', 'img': 'structures/Opera%20House.webp'}, {
        'name': 'Orphanage',
        'img': 'structures/Orphanage.webp',
    }, {'name': 'Palace', 'img': 'structures/Palace.webp'}, {
        'name': 'Palace (V&K)',
        'img': 'structures/Palace.webp',
    }, {'name': 'Park', 'img': 'structures/Park.webp'},
    {'name': 'Pier', 'img': 'structures/Pier.webp'},
    {'name': 'Rubble', 'img': 'structures/Rubble.webp'}, {
        'name': 'Sacred Grove',
        'img': 'structures/Sacred%20Grove.webp',
    }, {
        'name': 'Secure Warehouse',
        'img': 'structures/Secure%20Warehouse.webp',
    }, {
        'name': 'Shrine',
        'img': 'structures/Shrine.webp',
    }, {'name': 'Smithy', 'img': 'structures/Smithy.webp'}, {
        'name': 'Smithy (V&K)',
        'img': 'structures/Smithy.webp',
    }, {
        'name': 'Specialized Artisan',
        'img': 'structures/Special%20Artisan.webp',
    }, {'name': 'Stable', 'img': 'structures/Stable.webp'}, {
        'name': 'Stockyard',
        'img': 'structures/Stockyard.webp',
    }, {'name': 'Stonemason', 'img': 'structures/Stonemason.webp'}, {
        'name': 'Tannery',
        'img': 'structures/Tannery.webp',
    }, {
        'name': 'Tavern, Dive',
        'img': 'structures/Tavern%2C%20Dive.webp',
    }, {
        'name': 'Tavern, Dive (V&K)',
        'img': 'structures/Tavern%2C%20Dive.webp',
    }, {
        'name': 'Tavern, Luxury',
        'img': 'structures/Tavern%2C%20Luxury.webp',
    }, {
        'name': 'Tavern, Luxury (V&K)',
        'img': 'structures/Tavern%2C%20Luxury.webp',
    }, {
        'name': 'Tavern, Popular',
        'img': 'structures/Tavern%2C%20Popular.webp',
    }, {
        'name': 'Tavern, Popular (V&K)',
        'img': 'structures/Tavern%2C%20Popular.webp',
    }, {
        'name': 'Tavern, World-Class',
        'img': 'structures/Tavern%2C%20World%20Class.webp',
    }, {
        'name': 'Tavern, World-Class (V&K)',
        'img': 'structures/Tavern%2C%20World%20Class.webp',
    }, {'name': 'Temple', 'img': 'structures/Temple.webp'}, {
        'name': 'Tenement',
        'img': 'structures/Tenement.webp',
    }, {'name': 'Theater', 'img': 'structures/Theater.webp'}, {
        'name': 'Thieves\' Guild',
        'img': 'structures/Thieves%20Guild.webp',
    }, {'name': 'Town Hall', 'img': 'structures/Town%20Hall.webp'}, {
        'name': 'Town Hall (V&K)',
        'img': 'structures/Town%20Hall.webp',
    }, {'name': 'Trade Shop', 'img': 'structures/Trade%20Shop.webp'}, {
        'name': 'University',
        'img': 'structures/University.webp',
    }, {
        'name': 'Watchtower',
        'img': 'structures/Watchtower.webp',
    }, {
        'name': 'Watchtower, Stone',
        'img': 'structures/Watchtower.webp',
    }, {
        'name': 'Waterfront (Corner)',
        'img': 'structures/Waterfront%20(Corner).webp',
    }, {
        'name': 'Waterfront (Side)',
        'img': 'structures/Waterfront%20(Side).webp',
    }];

interface Mapping {
    name: string;
    img: string;
}

async function migrateActors(actors: Actor[], migrateToMappings: Mapping[]): Promise<void> {
    const mappingPerName = groupBy(migrateToMappings, (m) => m.name);
    await (Promise.all(actors.map(async (actor) => {
        const mapping = mappingPerName.get(actor.name ?? '')?.[0];
        if (mapping) {
            const img = mapping.img;
            await actor.update({'prototypeToken.texture.src': img, img});
        }
    })));
}

function getCustomMappings(baseDirectory = 'pf2e-kingmaker-tools-tokens/'): Mapping[] {
    return customMappings.map(m => {
        return {
            name: m.name,
            img: baseDirectory + m.img,
        };
    });
}

export async function structureTokenMappingDialog(game: Game): Promise<void> {
    const importedStructures = game.actors?.filter(a => isStructureActor(a)) ?? [];
    const journalLink = await TextEditor.enrichHTML(createUUIDLink('Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY', 'Kingmaker Tools Manual'));
    new Dialog({
        title: 'Change Structure Token Images',
        content: `
        <p>Run this macro to change all images of imported structures. The <b>Unofficial Module</b> mapping will use the folder structure described in the ${journalLink} Tokens section.</p> 
        <p>You can change the base directory from <b>pf2e-kingmaker-tools-tokens/</b> to a different folder by adding a different value in the Custom Directory input. That way you can make use of token modules which offers tokens organized in the same folder structure as the Unofficial Module.</p>
        <form class="simple-dialog-form">
            <div>
                <label for="km-token-mapping-official">Official Module</label>
                <input type="radio" value="official" name="mapping" id="km-token-mapping-official" checked>
            </div>
            <div>
                <label for="km-token-mapping-unofficial">Unofficial Module</label>
                <input type="radio" value="unofficial" name="mapping" id="km-token-mapping-unofficial">
            </div>
            <div>
                <label for="km-token-mapping-custom">Custom</label>
                <input type="radio" value="custom" name="mapping" id="km-token-mapping-custom">
            </div>
            <div>
                <label for="km-token-mapping-custom">Custom Directory</label>
                <input type="text" name="customDirectory" placeholder="modules/token-module/img/">
            </div>
        </form>
        `,
        buttons: {
            migrate: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Migrate',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const value = parseRadio($html, 'mapping');
                    const custom = parseTextInput($html, 'customDirectory');
                    if (value === 'custom') {
                        if (isBlank(custom)) {
                            ui.notifications?.error('Custom Directory can not be empty!');
                        } else {
                            // ensure trailing slash
                            const folder = custom.replace(/\/$/, '') + '/';
                            await migrateActors(importedStructures, getCustomMappings(folder));
                        }
                    } else if (value === 'official') {
                        await migrateActors(importedStructures, officialMappings);
                    } else if (value === 'unofficial') {
                        await migrateActors(importedStructures, getCustomMappings());
                    }
                },
            },
        },
        default: 'migrate',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}