import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../providers/auth_provider.dart';
import '../providers/theme_provider.dart';
import '../utils/app_theme.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios),
          onPressed: () => context.pop(),
        ),
        title: Text(
          'Settings',
          style: Theme.of(context).textTheme.headlineMedium,
        ),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Profile Section
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
                ),
              ),
              child: Row(
                children: [
                  Container(
                    width: 60,
                    height: 60,
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(30),
                    ),
                    child: Icon(
                      Icons.person,
                      size: 30,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Consumer<AuthProvider>(
                          builder: (context, authProvider, child) {
                            final user = authProvider.user;
                            return Text(
                              user?.displayName ?? user?.firstName ?? 'User',
                              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            );
                          },
                        ),
                        const SizedBox(height: 4),
                        Consumer<AuthProvider>(
                          builder: (context, authProvider, child) {
                            final user = authProvider.user;
                            return Text(
                              user?.email ?? 'user@example.com',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: Theme.of(context).colorScheme.onSurface.withOpacity(0.7),
                              ),
                            );
                          },
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.edit_outlined),
                    onPressed: () {
                      context.pop(); // Go back to profile
                    },
                  ),
                ],
              ),
            ).animate().fadeIn(duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 32),
            
            // Appearance Section
            _buildSectionHeader('Appearance', Icons.palette_outlined, 800),
            
            const SizedBox(height: 20),
            
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
                ),
              ),
              child: Column(
                children: [
                  // Theme Mode
                  Row(
                    children: [
                      Icon(
                        Icons.brightness_6_outlined,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Theme Mode',
                              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            Text(
                              'Choose your preferred theme',
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                              ),
                            ),
                          ],
                        ),
                      ),
                      Consumer<ThemeProvider>(
                        builder: (context, themeProvider, child) {
                          return DropdownButton<ThemeMode>(
                            value: themeProvider.themeMode,
                            onChanged: (ThemeMode? newValue) {
                              if (newValue != null) {
                                themeProvider.setThemeMode(newValue);
                              }
                            },
                            items: const [
                              DropdownMenuItem(
                                value: ThemeMode.system,
                                child: Text('System'),
                              ),
                              DropdownMenuItem(
                                value: ThemeMode.light,
                                child: Text('Light'),
                              ),
                              DropdownMenuItem(
                                value: ThemeMode.dark,
                                child: Text('Dark'),
                              ),
                            ],
                            underline: Container(),
                            icon: Icon(
                              Icons.keyboard_arrow_down,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                          );
                        },
                      ),
                    ],
                  ),
                ],
              ),
            ).animate().fadeIn(delay: 1000.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 32),
            
            // Account Section
            _buildSectionHeader('Account', Icons.account_circle_outlined, 1200),
            
            const SizedBox(height: 20),
            
            _buildSettingsCard(
              icon: Icons.security_outlined,
              title: 'Security & Privacy',
              subtitle: 'Manage your account security',
              onTap: () {
                // TODO: Implement security settings
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Security settings coming soon!'),
                    backgroundColor: Colors.blue,
                  ),
                );
              },
            ).animate().fadeIn(delay: 1400.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 16),
            
            _buildSettingsCard(
              icon: Icons.notifications_outlined,
              title: 'Notifications',
              subtitle: 'Configure notification preferences',
              onTap: () {
                // TODO: Implement notification settings
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Notification settings coming soon!'),
                    backgroundColor: Colors.blue,
                  ),
                );
              },
            ).animate().fadeIn(delay: 1600.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 16),
            
            _buildSettingsCard(
              icon: Icons.language_outlined,
              title: 'Language',
              subtitle: 'Change app language',
              onTap: () {
                // TODO: Implement language settings
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Language settings coming soon!'),
                    backgroundColor: Colors.blue,
                  ),
                );
              },
            ).animate().fadeIn(delay: 1800.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 32),
            
            // Support Section
            _buildSectionHeader('Support', Icons.help_outline, 2000),
            
            const SizedBox(height: 20),
            
            _buildSettingsCard(
              icon: Icons.help_outline,
              title: 'Help & FAQ',
              subtitle: 'Get help and find answers',
              onTap: () {
                // TODO: Implement help section
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Help section coming soon!'),
                    backgroundColor: Colors.blue,
                  ),
                );
              },
            ).animate().fadeIn(delay: 2200.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 16),
            
            _buildSettingsCard(
              icon: Icons.feedback_outlined,
              title: 'Send Feedback',
              subtitle: 'Help us improve the app',
              onTap: () {
                // TODO: Implement feedback
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Feedback feature coming soon!'),
                    backgroundColor: Colors.blue,
                  ),
                );
              },
            ).animate().fadeIn(delay: 2400.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 16),
            
            _buildSettingsCard(
              icon: Icons.info_outline,
              title: 'About',
              subtitle: 'App version and information',
              onTap: () {
                _showAboutDialog();
              },
            ).animate().fadeIn(delay: 2600.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 40),
            
            // Logout Button
            SizedBox(
              height: 56,
              child: OutlinedButton(
                onPressed: () => _showLogoutDialog(),
                style: OutlinedButton.styleFrom(
                  side: BorderSide(
                    color: Theme.of(context).colorScheme.error,
                  ),
                ),
                child: Text(
                  'Sign Out',
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.error,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ).animate().fadeIn(delay: 2800.ms, duration: 600.ms).slideY(begin: 0.3),
            
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title, IconData icon, int delay) {
    return Row(
      children: [
        Icon(
          icon,
          color: Theme.of(context).colorScheme.primary,
          size: 20,
        ),
        const SizedBox(width: 8),
        Text(
          title,
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
            color: Theme.of(context).colorScheme.primary,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    ).animate().fadeIn(delay: delay.ms, duration: 600.ms).slideX(begin: -0.3);
  }

  Widget _buildSettingsCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primary.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(
                icon,
                color: Theme.of(context).colorScheme.primary,
                size: 20,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                    ),
                  ),
                ],
              ),
            ),
            Icon(
              Icons.chevron_right,
              color: Theme.of(context).colorScheme.onSurface.withOpacity(0.4),
            ),
          ],
        ),
      ),
    );
  }

  void _showAboutDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('About StoneRidge'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Version: 1.0.0'),
              const SizedBox(height: 8),
              Text('A community marketplace app for StoneRidge residents.'),
              const SizedBox(height: 16),
              Text('© 2024 StoneRidge Marketplace. All rights reserved.'),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => context.pop(),
              child: const Text('Close'),
            ),
          ],
        );
      },
    );
  }

  void _showLogoutDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Sign Out'),
          content: const Text('Are you sure you want to sign out?'),
          actions: [
            TextButton(
              onPressed: () => context.pop(),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () async {
                context.pop();
                await _handleLogout();
              },
              child: Text(
                'Sign Out',
                style: TextStyle(
                  color: Theme.of(context).colorScheme.error,
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Future<void> _handleLogout() async {
    final authProvider = context.read<AuthProvider>();
    await authProvider.logout();
    
    if (mounted) {
      context.go('/login');
    }
  }
}
