import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../screens/splash_screen.dart';
import '../screens/onboarding_screen.dart';
import '../screens/login_screen.dart';
import '../screens/registration_screen.dart';
import '../screens/verification_screen.dart';
import '../screens/home_screen.dart';
import '../screens/profile_screen.dart';
import '../screens/settings_screen.dart';

class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final authProvider = context.read<AuthProvider>();
      
      // If we're on the splash screen, let it handle its own navigation
      if (state.uri.path == '/') {
        return null;
      }
      
      // Wait for token loading to complete before making redirect decisions
      if (authProvider.isTokenLoading) {
        print('Router: Waiting for token loading to complete...');
        return null; // No redirect while loading
      }
      
      print('Router: Checking auth state - hasStoredTokens: ${authProvider.hasStoredTokens}');
      
      // If we're on protected routes without tokens, redirect to login
      if (_isProtectedRoute(state.uri.path) && !authProvider.hasStoredTokens) {
        print('Router: Redirecting from protected route to login (no tokens)');
        return '/login';
      }
      
      // If we're on auth routes with tokens, redirect to home
      if (_isAuthRoute(state.uri.path) && authProvider.hasStoredTokens) {
        print('Router: Redirecting from auth route to home (user has tokens)');
        return '/home';
      }
      
      print('Router: No redirect needed');
      return null; // No redirect needed
    },
    routes: [
      GoRoute(
        path: '/',
        name: 'splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        name: 'onboarding',
        builder: (context, state) => const OnboardingScreen(),
      ),
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        name: 'register',
        builder: (context, state) => const RegistrationScreen(),
      ),
      GoRoute(
        path: '/verify',
        name: 'verify',
        builder: (context, state) => const VerificationScreen(),
      ),
      GoRoute(
        path: '/home',
        name: 'home',
        builder: (context, state) => const HomeScreen(),
      ),
      GoRoute(
        path: '/profile',
        name: 'profile',
        builder: (context, state) => const ProfileScreen(),
      ),
      GoRoute(
        path: '/settings',
        name: 'settings',
        builder: (context, state) => const SettingsScreen(),
      ),
    ],
  );

  static bool _isProtectedRoute(String location) {
    return location.startsWith('/home') || 
           location.startsWith('/profile') || 
           location.startsWith('/settings');
  }

  static bool _isAuthRoute(String location) {
    return location.startsWith('/login') || 
           location.startsWith('/register') || 
           location.startsWith('/verify') ||
           location.startsWith('/onboarding');
  }
}
