@file:JsQualifier("foundry.canvas.layers")
package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.canvas.placeables.Token
import com.foundryvtt.core.documents.TokenDocument

external class TokenLayer : PlaceablesLayer<TokenDocument, Token>