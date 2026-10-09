package com.fireflow.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fireflow.common.components.BOTTOM_NAV_ROUTES
import com.fireflow.common.components.FireFlowBottomBar

object Routes {
    const val LOGIN = "login"
    const val SETUP = "setup"
    const val CHANGE_PASSWORD = "change_password"
    const val CLIENTS = "clients"
    const val CLIENT_DETAIL = "client/{clientId}"
    const val CLIENT_FORM = "client/form?clientId={clientId}"
    const val GROUPS = "client/{clientId}/groups"
    const val GROUP_DETAIL = "client/{clientId}/group/{groupId}"
    const val GROUP_FORM = "client/{clientId}/group/form?groupId={groupId}"
    const val MOTOR_FORM = "client/{clientId}/group/{groupId}/motor/form"
    const val REVISIONS = "client/{clientId}/group/{groupId}/revisions"
    const val REVISION_DETAIL = "revision/{revisionId}"
    const val REVISION_FORM = "client/{clientId}/group/{groupId}/revision/form?revisionId={revisionId}"
    const val CURVAS = "revision/{revisionId}/curvas"
    const val CONVERTERS = "converters"
    const val PHOTOS = "revision/{revisionId}/photos"
    const val REPORTS = "revision/{revisionId}/report"
    const val EXPORTS = "revision/{revisionId}/export"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    fun clientDetail(clientId: Long) = "client/$clientId"
    fun clientForm(clientId: Long? = null) = "client/form?clientId=${clientId ?: ""}"
    fun groups(clientId: Long) = "client/$clientId/groups"
    fun groupDetail(clientId: Long, groupId: Long) = "client/$clientId/group/$groupId"
    fun groupForm(clientId: Long, groupId: Long? = null) = "client/$clientId/group/form?groupId=${groupId ?: ""}"
    fun motorForm(clientId: Long, groupId: Long) = "client/$clientId/group/$groupId/motor/form"
    fun revisions(clientId: Long, groupId: Long) = "client/$clientId/group/$groupId/revisions"
    fun revisionDetail(revisionId: Long) = "revision/$revisionId"
    fun revisionForm(clientId: Long, groupId: Long, revisionId: Long? = null) = "client/$clientId/group/$groupId/revision/form?revisionId=${revisionId ?: ""}"
    fun curvas(revisionId: Long) = "revision/$revisionId/curvas"
    fun photos(revisionId: Long) = "revision/$revisionId/photos"
    fun reports(revisionId: Long) = "revision/$revisionId/report"
    fun exports(revisionId: Long) = "revision/$revisionId/export"
}

private const val ANIM_DURATION = 300

@Composable
fun FireFlowNavHost(
    navController: NavHostController = rememberNavController()
) {
    val startDestinationViewModel: StartDestinationViewModel = hiltViewModel()
    val startDestination by startDestinationViewModel.startDestination.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES

    val destination = startDestination
    if (destination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                FireFlowBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.CLIENTS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = destination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Routes.SETUP,
                enterTransition = { fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.login.SetupScreen(
                    onSetupComplete = {
                        navController.navigate(Routes.CLIENTS) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.LOGIN,
                enterTransition = { fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.login.LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.CLIENTS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onChangePassword = {
                        navController.navigate(Routes.CHANGE_PASSWORD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.CHANGE_PASSWORD,
                enterTransition = { fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.login.ChangePasswordScreen(
                    onPasswordChanged = {
                        navController.navigate(Routes.CLIENTS) {
                            popUpTo(Routes.CHANGE_PASSWORD) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.CLIENTS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.clientes.ClientesScreen(
                    onClientClick = { clientId ->
                        navController.navigate(Routes.clientDetail(clientId))
                    },
                    onAddClient = {
                        navController.navigate(Routes.clientForm())
                    }
                )
            }

            composable(
                route = Routes.CLIENT_DETAIL,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                com.fireflow.clientes.ClienteDetailScreen(
                    clientId = clientId,
                    onGroupClick = { groupId ->
                        navController.navigate(Routes.groupDetail(clientId, groupId))
                    },
                    onAddGroup = {
                        navController.navigate(Routes.groupForm(clientId))
                    },
                    onEditClient = {
                        navController.navigate(Routes.clientForm(clientId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CLIENT_FORM,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull()
                com.fireflow.clientes.ClienteFormScreen(
                    clientId = clientId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.GROUPS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                com.fireflow.grupos.GruposScreen(
                    clientId = clientId,
                    onGroupClick = { groupId ->
                        navController.navigate(Routes.groupDetail(clientId, groupId))
                    },
                    onAddGroup = {
                        navController.navigate(Routes.groupForm(clientId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.GROUP_DETAIL,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: return@composable
                com.fireflow.grupos.GrupoDetailScreen(
                    clientId = clientId,
                    groupId = groupId,
                    onRevisionClick = { revisionId ->
                        navController.navigate(Routes.revisionDetail(revisionId))
                    },
                    onAddRevision = {
                        navController.navigate(Routes.revisionForm(clientId, groupId))
                    },
                    onEditMotors = {
                        navController.navigate(Routes.motorForm(clientId, groupId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.GROUP_FORM,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull()
                com.fireflow.grupos.GrupoFormScreen(
                    clientId = clientId,
                    groupId = groupId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.MOTOR_FORM,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: return@composable
                com.fireflow.grupos.MotorFormScreen(
                    clientId = clientId,
                    groupId = groupId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.REVISIONS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: return@composable
                com.fireflow.revisiones.RevisionesScreen(
                    clientId = clientId,
                    groupId = groupId,
                    onRevisionClick = { revisionId ->
                        navController.navigate(Routes.revisionDetail(revisionId))
                    },
                    onAddRevision = {
                        navController.navigate(Routes.revisionForm(clientId, groupId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.REVISION_DETAIL,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull() ?: return@composable
                com.fireflow.revisiones.RevisionDetailScreen(
                    revisionId = revisionId,
                    onCurvasClick = { id -> navController.navigate(Routes.curvas(id)) },
                    onPhotosClick = { id -> navController.navigate(Routes.photos(id)) },
                    onReportClick = { id -> navController.navigate(Routes.reports(id)) },
                    onExportClick = { id -> navController.navigate(Routes.exports(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.REVISION_FORM,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toLongOrNull() ?: return@composable
                val groupId = backStackEntry.arguments?.getString("groupId")?.toLongOrNull() ?: return@composable
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull()
                com.fireflow.revisiones.RevisionFormScreen(
                    clientId = clientId,
                    groupId = groupId,
                    revisionId = revisionId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CURVAS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull() ?: return@composable
                com.fireflow.curvas.CurvasScreen(
                    revisionId = revisionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CONVERTERS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.conversiones.ConverterScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.PHOTOS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull() ?: return@composable
                com.fireflow.fotografias.PhotosScreen(
                    revisionId = revisionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.REPORTS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull() ?: return@composable
                com.fireflow.informes.ReportScreen(
                    revisionId = revisionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EXPORTS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) { backStackEntry ->
                val revisionId = backStackEntry.arguments?.getString("revisionId")?.toLongOrNull() ?: return@composable
                com.fireflow.exportaciones.ExportScreen(
                    revisionId = revisionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.HISTORY,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.historial.HistoryScreen(
                    onRevisionClick = { revisionId ->
                        navController.navigate(Routes.revisionDetail(revisionId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.SETTINGS,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeIn(tween(ANIM_DURATION)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(ANIM_DURATION)) + fadeOut(tween(ANIM_DURATION)) }
            ) {
                com.fireflow.configuracion.SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
