package com.foundryvtt.core.documents

import com.foundryvtt.core.abstract.DataModel

external class PrototypeToken: DataModel {
    var actorLink: Boolean
    var texture: TextureData
}