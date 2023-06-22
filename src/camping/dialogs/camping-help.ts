const content = `
<div>
    <h2>Usage</h2>
    <p>The basic idea of the sheet is to make camping incredibly fast by:</p>
    <ul>
        <li>keeping previous camp assignments</li>
        <li>showing players and GMs if all activities have been performed</li>
        <li>automatically managing Camping and Meal effects</li>
        <li>automating resource management like rations and ingredients</li>
        <li>easily override degrees of successes</li>
    </ul>
    <p>First, drag the <b>Camping Sheet</b> Macro into your macro bar and run it once. This will create an actor called <b>Camping Sheet</b> in your actor directory. Do not rename or delete it since it will be used to store and retrieve data. The data is persisted on the actor's flags property which is not visible to users by opening the default actor sheet.</p>
    
    <p>Once the actor exists, you set up the camping sheet by drag and dropping actors from your actor tab into the <b>In Camp</b> sidebar. These actors are considered to be available for watches, eating and activity selection.</p>
    
    <h3>Random Encounters</h3>
    <p>Random encounters are managed at the very top by selecting the region drop down, changing the flat check modifier if necessary and then clicking the <i class="fa-solid fa-skull"></i><i class="fa-solid fa-dice-d20"></i> button. If you don't want to perform a flat check hit the <i class="fa-solid fa-skull"></i> button</p>
    
    <p>Keep in mind that the encounter DC is not only modified by the encounter modifier, but also by chosen activities like <b>Blend into the Night</b>. Furthermore, passing time using the <b>+1/2h</b> buttons will not automatically increase the modifier, nor roll a random encounter. Rolling random encounters will not reset the modifier to 0.</p>
    
    <h3>Camping Activities</h3>
    <p>From the sidebar, you can drag and drop actors onto activities. If they don't fulfill the skill requirements, you will get an error notification.</p> 
    <p>In order to start a camping session, you have to set <b>Prepare Campsite</b> to a result other than <b>Critical Failure</b> or <b>-</b>. This opens up the remaining, enabled activities.</p>
    
    <p>Most of the existing activities are hidden and can be enabled by clicking the <b>Unlock</b> button. This allows you to selectively enable actions related to companions.</p>
    
    <p>PCs can now drop their actor onto the desired activity, select the designated skill and perform the check. The result is immediately populated into the result drop down below and shown in the sidebar using different colors for the degrees. This allows you to quickly check how many PCs still need to perform an activity. Activities without checks will show up in the sidebar as if they had a critical success.</p>
    
    <p>After an activity result has been chosen, its effects are automatically applied to PCs. For this reason you want to complete actions like <b>Tell Campfire Story</b> before performing other activities. You can also quickly choose a different result. The previous effects will be removed if necessary, new ones will be applied immediately.</p>
   
   <p>Some actions like Amiri's <b>Enhance Weapons</b> only apply to combat and can't be easily applied to PCs. You can post them into chat using the <b>Combat Effects to Chat</b> button, where you can now drag and drop them onto PCs or onto enemies</p>
    
    <p>If you want to apply other effects from chat or add items to PC's inventories, you can also drag and drop them onto their profile pictures in the sidebar, activity list or eating sections.</p>
    
    <p>If you want to completely remove all selected activities and their results, hit the <b>Clear</b> button.</p>
    
    <p>At the end of a rest, all camping effects will be removed from the actors in the sidebar.</p>
    
    <h3>Eating</h3>
    <p>Each character in camp is available in the <b>Eating</b> section where you can choose their food for the day, change their favorite meal or even roll a Survival <b>Subsist</b> check. If you wish to forgo tracking rations or meals for that character, set the meal to <b>Nothing</b>. <b>Ration/Subsistence</b> will first reduce Magical Subsistence from spells like <b>Create Food</b>, then normal Subsistence from actions like Subsist, then available rations from players' inventories.</p>
    
    <p>If a PC has chosen the <b>Cook Meal</b> activity, you will be able to select a recipe and a skill to cook it. Rolling a check will automatically apply fill in the degree of success. Upon changing this value, all expired and previously applied meal effects will be removed from every actor. Then all meal effects related to the recipe and degree of success will be applied to the actors that have chosen the <b>Meal</b> value.</p>
    
    <p>By default, none of the ingredients nor rations will be removed from players' inventories. Hitting the <i class="fa-solid fa-minus"></i> button in the <b>Pay</b> column will remove the appropriate number of ingredients and items from character inventories that are present in the sidebar. If you are lacking ingredients and rations, values in the <b>Consumed</b> row will be shown in red. Paying while not having enough resources will reduce as many rations and ingredients as possible, but will have no further effect.</p>
    
    <p>Values set in the <b>Subsistence</b> and <b>Magical Subsistence</b> fields will not be touched, since casting <b>Create Food</b> and successfully executing <b>Subsist</b> will become the default after level 5 if your players optimize. Before that, you will need to manually change these values each camping session.</p>
    
    <h3>Resting</h3>
    <p>You can start a rest by hitting the <b>Begin Rest</b> button. By default, one random encounter will be rolled automatically per rest. If the encounter occurs, a random point in time between now and the end of the rest will be chosen and time will automatically advance to that point in time. Afterward, a random actor that is present in the sidebar will be chosen as having watch. A secret perception check is then rolled in chat.</p>
    
    <p>Once you finish the random encounter, you can continue the rest by pressing <b>Continue Rest</b>. No more random encounters will happen. Time will advance to after your daily preparations and the <b>Adventuring</b> time tracker will reset to 0.</p>
    
    <p>Then the following things happen:</p>
    <ul>
        <li>All camping effects will be removed</li>
        <li>All meal effects that expire after resting will be removed</li>
        <li>The <b>Rest for the Night</b> macro will be executed for all actors in the sidebar</li>
        <li>Additional healing received from <b>Basic Meal</b> and <b>Dawnflower's Blessing</b> will be applied</li>
        <li>All camping activity results are reset to <b>-</b></li>
    </ul>
    
    <h2>Automation</h2>
    <p>The camping rules are implemented as RAW and flexible as feasible. Compared to RAW, the following rules are not automated:</p>
    
    <h3>General</h3>
    <ul>
        <li>The encounter DC modifier is not being increased/decreased/reset automatically. Many people have issues with frequency and likelihood of Random Encounter checks, so we leave this up to the GM.</li>
        <li>The rules mention that each PC can take exploration actions as desired during Prepare Campsite and Camping Activities. Since these are GM fiat, they aren't implemented on the sheet. Simply open the character sheet and roll the checks manually, choosing the appropriate modifiers</li>
        <li>Fatigued is not applied automatically since there are so many ways to increase or forgo tracking it.</li>
    </ul>
    
    <h3>Camping Activities</h3>
    <ul>
        <li><b>Relax</b>: Does not remove the penalty from a Tell Campfire Story Critical Failure activity nor receive any different benefits from Tell Campfire Story Success and Critical Success results. Reason for this is that this requires a way to track when effects were performed which not only leads to a lot of work but also reduces the sheet's usability by a significant amount.</b></li>
        <li><b>Camp Management</b>: There is no concept of sequential activities</li>
        <li><b>Exploration Activities</b>: Activities that can be taken by more than one PC such as Exploration Activities are not supported since they require a different way of tracking time and have no clear way of automating them. You can execute these directly from your PC's sheet instead</li>
    </ul>
    
    <h3>Eating</h3>
    <ul>
        <li>You can only cook one recipe right now because it requires a significant amount of work to implement talking multiple actions and cooking multiple recipes</li>
        <li>Subsist is not automated since there are feats like Forager or items like Coyote cloak that modify the amount of rations</li>
        <li>Starvation is not tracked</li>
    </ul>
    
    <h3>Meals</h3>
    <p>The following meals are not fully automated:</p>
    <ul>
        <li><b>Hunter's Roast</b>: Critical Failure Does not roll poison damage</li>
        <li><b>Kameberry Pie</b>: Does not restore any hit points</li>
        <li><b>Mastodon Steak</b>: Does not restore any hit points nor reduce conditions</li>
        <li><b></b>Shepherd's Pie</b>: Does not restore any hit points nor reduce conditions. A critical failure won't reduce healing by half.</li> 
        <li><b>Smoked Trout And Hydra Pate</b>: Does not add the comfort trait to light armors</li>
        <li><b>Succulent Sausages</b>: Does not reduce flat checks to end persistent damage</li>
    </ul>
    <p>The following meals lack the Poison trait:</p>
    <ul>
        <li>Baked Spider Critical Failure</li>
        <li>Broiled Tuskwater Oysters Critical Failure</li>
        <li>Grilled Silver Eel Critical Failure</li>
        <li>Haggis Critical Failure</li>
        <li>Hearty Purple Soup Critical Failure</li>
        <li>Mastodon Steak Critical Failure</li>
        <li>Onion Soup Critical Failure</li>
        <li>Owlbear Omelet Critical Failure</li>
        <li>Seasoned Wings And Thighs Critical Failure</li>
        <li>Smoked Trout And Hydra Pate Critical Failure</li>
        <li>Succulent Sausages Critical Failure</li>
        <li>Sweet Pancakes Critical Failure</li>
    </ul>
    <p>The following meals lack the Curse trait:</p>
    <ul>
        <li>Black Linnorm Stew: Critical Failure</li>
        <li>First World Mince Pie: Critical Failure</li>
        <li>Rice-N-Nut Pudding: Critical Failure</li>
    </ul>
</div>`;

export function showCampingHelp(): void {
    new Dialog({
        title: 'Camping',
        content,
        buttons: {},
    }, {
        width: 600,
        height: 800,
        jQuery: false,
    }).render(true);
}
