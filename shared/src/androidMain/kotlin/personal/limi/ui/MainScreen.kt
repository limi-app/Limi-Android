package personal.limi.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.vicart.compose.material.symbols.MaterialSymbols
import dev.vicart.compose.material.symbols.OutlinedSymbol
import limi.shared.generated.resources.Res
import limi.shared.generated.resources.home
import limi.shared.generated.resources.rule
import limi.shared.generated.resources.setting
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import personal.limi.ui.screen.HomeScreen
import personal.limi.ui.screen.RuleScreen
import personal.limi.ui.screen.SettingScreen

enum class MainScreen(val titleResId: StringResource, val icon: String) {
  Home(
    titleResId = Res.string.home,
    icon = MaterialSymbols.HOME,
  ),
  Rule(
    titleResId = Res.string.rule,
    icon = MaterialSymbols.LIST_ALT,
  ),
  Setting(titleResId = Res.string.setting, icon = MaterialSymbols.SETTINGS),
}

@Composable
@Preview
fun LimiApp(
  viewModel: MainViewModel = viewModel { MainViewModel() },
  navController: NavHostController = rememberNavController(),
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: MainScreen.Home.name
  val currentScreen = MainScreen.valueOf(currentRoute)

  Scaffold(
    bottomBar = {
      AppNavBar(currentScreen, navController)
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = MainScreen.Home.name,
      modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
    ) {
      MainScreen.entries.forEach { screen ->
        composable(
          route = screen.name,
          enterTransition = {
            val isForward =
              getScreenIndex(targetState.destination.route) >
                getScreenIndex(initialState.destination.route)

            slideIntoContainer(
              towards =
                if (isForward) AnimatedContentTransitionScope.SlideDirection.Start
                else AnimatedContentTransitionScope.SlideDirection.End,
              animationSpec = tween(durationMillis = 300),
            )
          },
          exitTransition = {
            val isForward =
              getScreenIndex(targetState.destination.route) >
                getScreenIndex(initialState.destination.route)

            slideOutOfContainer(
              towards =
                if (isForward) AnimatedContentTransitionScope.SlideDirection.Start
                else AnimatedContentTransitionScope.SlideDirection.End,
              animationSpec = tween(durationMillis = 300),
            )
          },
        ) {
          when (screen) {
            MainScreen.Home -> HomeScreen()
            MainScreen.Rule -> RuleScreen(viewModel, MainScreen.Rule.titleResId)
            MainScreen.Setting -> SettingScreen(viewModel, MainScreen.Setting.titleResId)
          }
        }
      }
    }
  }
}

@Composable
fun AppNavBar(currentScreen: MainScreen, navController: NavHostController) {
  NavigationBar {
    MainScreen.entries.forEach { screen ->
      NavigationBarItem(
        icon = { OutlinedSymbol(icon = screen.icon, weight = FontWeight.Normal) },
        label = { Text(stringResource(screen.titleResId)) },
        selected = currentScreen == screen,
        onClick = {
          navController.navigate(screen.name) {
            popUpTo(navController.graph.startDestinationId) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        },
      )
    }
  }
}

// 根据路由名称获取屏幕的索引
private fun getScreenIndex(route: String?): Int {
  return MainScreen.entries.indexOfFirst { it.name == route }
}
