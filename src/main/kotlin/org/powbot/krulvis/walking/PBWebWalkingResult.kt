package org.powbot.krulvis.walking

import org.powbot.walking.WebWalkingResult

enum class FailureReason {
    NoPath, FailedInteract, CantReachNextNode, CantLoadPlayer, ExceptionThrown, TargetNull, Unknown
}
data class PBWebWalkingResult(val usedWeb: Boolean,
                              val success: Boolean,
                              val failureReason: FailureReason?)
