import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> with TickerProviderStateMixin {
  bool _hasNavigated = false;

  @override
  void initState() {
    super.initState();
    _handleNavigation();
  }

  void _handleNavigation() {
    // Add a post frame callback to ensure the widget is fully built
    WidgetsBinding.instance.addPostFrameCallback((_) {
      // Listen to auth provider changes
      final authProvider = context.read<AuthProvider>();
      
      // If tokens are already loaded, navigate immediately
      if (!authProvider.isTokenLoading) {
        _navigateBasedOnAuthState(authProvider);
        return;
      }
      
      // Listen for auth state changes
      authProvider.addListener(() {
        if (!_hasNavigated && !authProvider.isTokenLoading) {
          _navigateBasedOnAuthState(authProvider);
        }
      });
      
      // Fallback: navigate after 5 seconds regardless
      Future.delayed(const Duration(seconds: 5), () {
        if (!_hasNavigated) {
          _navigateBasedOnAuthState(authProvider);
        }
      });
    });
  }

  void _navigateBasedOnAuthState(AuthProvider authProvider) {
    if (_hasNavigated) return;
    _hasNavigated = true;
    
    if (authProvider.hasStoredTokens) {
      // User has tokens, go to home
      context.go('/home');
    } else {
      // No tokens, go to onboarding
      context.go('/onboarding');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // App Logo/Icon
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(30),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.1),
                    blurRadius: 20,
                    offset: const Offset(0, 10),
                  ),
                ],
              ),
              child: Icon(
                Icons.store,
                size: 60,
                color: Theme.of(context).colorScheme.primary,
              ),
            ).animate().fadeIn(duration: 800.ms).scale(),
            
            const SizedBox(height: 40),
            
            // App Name
            Text(
              'StoneRidge',
              style: Theme.of(context).textTheme.displayLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ).animate().fadeIn(delay: 400.ms, duration: 800.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 8),
            
            Text(
              'Marketplace',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                color: Colors.white.withOpacity(0.9),
                fontWeight: FontWeight.w500,
              ),
            ).animate().fadeIn(delay: 600.ms, duration: 800.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 60),
            
            // Loading Indicator
            SizedBox(
              width: 40,
              height: 40,
              child: CircularProgressIndicator(
                strokeWidth: 3,
                valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
              ),
            ).animate().fadeIn(delay: 800.ms, duration: 600.ms).scale(),
          ],
        ),
      ),
    );
  }
}
