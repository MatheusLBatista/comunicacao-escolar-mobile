package dev.fslab.comunicacao.escolar.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * NavigationExtensions — Extensões de navegação segura anti-crash.
 *
 * Proteção contra:
 * - Duplo clique que navega duas vezes
 * - Race conditions entre back-stack e composição
 */

private object NavigationThrottle {
    private const val DEBOUNCE_MS = 300L
    private var lastNavTime = 0L

    fun canNavigate(): Boolean = System.currentTimeMillis() - lastNavTime > DEBOUNCE_MS

    fun recordNavigation() {
        lastNavTime = System.currentTimeMillis()
    }
}

/**
 * Navega para [route] somente se o ciclo de vida atual for RESUMED
 * e não houver uma navegação recente (debounce de 300ms).
 */
fun NavController.navigateSafely(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = { launchSingleTop = true }
) {
    val currentEntry = currentBackStackEntry
    if (currentEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
        && NavigationThrottle.canNavigate()
    ) {
        NavigationThrottle.recordNavigation()
        navigate(route, builder)
    }
}

/**
 * Volta na back-stack somente se o ciclo de vida atual for RESUMED.
 */
fun NavController.popBackStackSafely(): Boolean {
    val currentEntry = currentBackStackEntry
    return if (currentEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
        popBackStack()
    } else {
        false
    }
}
