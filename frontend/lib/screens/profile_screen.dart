import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../providers/auth_provider.dart';
import '../utils/app_theme.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _firstNameController = TextEditingController();
  final _lastNameController = TextEditingController();
  final _displayNameController = TextEditingController();
  final _emailController = TextEditingController();
  final _apartmentController = TextEditingController();
  final _buildingController = TextEditingController();
  final _phoneController = TextEditingController();
  
  bool _isEditing = false;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadUserData();
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _displayNameController.dispose();
    _emailController.dispose();
    _apartmentController.dispose();
    _buildingController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  void _loadUserData() {
    final authProvider = context.read<AuthProvider>();
    final user = authProvider.user;
    
    if (user != null) {
      _firstNameController.text = user.firstName ?? '';
      _lastNameController.text = user.lastName ?? '';
      _displayNameController.text = user.displayName ?? '';
      _emailController.text = user.email ?? '';
      _apartmentController.text = user.apartmentNumber ?? '';
      _buildingController.text = user.buildingName ?? '';
      _phoneController.text = user.phoneNumber ?? '';
    }
  }

  Future<void> _saveProfile() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
    });

    final authProvider = context.read<AuthProvider>();
    final success = await authProvider.updateProfile(
      firstName: _firstNameController.text.trim(),
      lastName: _lastNameController.text.trim(),
      displayName: _displayNameController.text.trim(),
      email: _emailController.text.trim(),
      apartmentNumber: _apartmentController.text.trim(),
      buildingName: _buildingController.text.trim(),
      phoneNumber: _phoneController.text.trim(),
    );

    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Profile updated successfully!'),
          backgroundColor: Colors.green,
        ),
      );
      setState(() {
        _isEditing = false;
      });
    }

    if (mounted) {
      setState(() {
        _isLoading = false;
      });
    }
  }

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
          'Profile',
          style: Theme.of(context).textTheme.headlineMedium,
        ),
        actions: [
          if (!_isEditing)
            IconButton(
              icon: const Icon(Icons.edit_outlined),
              onPressed: () {
                setState(() {
                  _isEditing = true;
                });
              },
            ),
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () {
              context.push('/settings');
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Profile Header
              Consumer<AuthProvider>(
                builder: (context, authProvider, child) {
                  final user = authProvider.user;
                  return Container(
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          Theme.of(context).colorScheme.primary,
                          Theme.of(context).colorScheme.primary.withOpacity(0.8),
                        ],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Column(
                      children: [
                        // Profile Avatar
                        Container(
                          width: 100,
                          height: 100,
                          decoration: BoxDecoration(
                            color: Colors.white.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(50),
                            border: Border.all(
                              color: Colors.white.withOpacity(0.3),
                              width: 3,
                            ),
                          ),
                          child: Icon(
                            Icons.person,
                            size: 50,
                            color: Colors.white,
                          ),
                        ).animate().fadeIn(duration: 600.ms).scale(),
                        
                        const SizedBox(height: 20),
                        
                        // User Info
                        Text(
                          user?.displayName ?? user?.firstName ?? 'User',
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ).animate().fadeIn(delay: 200.ms, duration: 600.ms).slideY(begin: 0.3),
                        
                        const SizedBox(height: 8),
                        
                        Text(
                          '@${user?.username ?? 'username'}',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            color: Colors.white.withOpacity(0.9),
                          ),
                        ).animate().fadeIn(delay: 400.ms, duration: 600.ms).slideY(begin: 0.3),
                        
                        const SizedBox(height: 16),
                        
                        // Verification Status
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              user?.emailVerified == true 
                                  ? Icons.verified 
                                  : Icons.pending,
                              color: user?.emailVerified == true 
                                  ? Colors.green 
                                  : Colors.orange,
                              size: 20,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              user?.emailVerified == true 
                                  ? 'Email Verified' 
                                  : 'Email Pending',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: Colors.white.withOpacity(0.9),
                              ),
                            ),
                          ],
                        ).animate().fadeIn(delay: 600.ms, duration: 600.ms).slideY(begin: 0.3),
                      ],
                    ),
                  );
                },
              ),
              
              const SizedBox(height: 32),
              
              // Profile Form
              if (_isEditing) ...[
                _buildSectionHeader('Personal Information', Icons.person_outline, 800),
                
                const SizedBox(height: 20),
                
                // First Name & Last Name Row
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _firstNameController,
                        enabled: _isEditing,
                        decoration: const InputDecoration(
                          labelText: 'First Name *',
                          hintText: 'Enter first name',
                        ),
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return 'First name is required';
                          }
                          return null;
                        },
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _lastNameController,
                        enabled: _isEditing,
                        decoration: const InputDecoration(
                          labelText: 'Last Name',
                          hintText: 'Enter last name',
                        ),
                      ),
                    ),
                  ],
                ).animate().fadeIn(delay: 1000.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 20),
                
                // Display Name
                TextFormField(
                  controller: _displayNameController,
                  enabled: _isEditing,
                  decoration: const InputDecoration(
                    labelText: 'Display Name',
                    hintText: 'How others will see you',
                    prefixIcon: Icon(Icons.badge_outlined),
                  ),
                ).animate().fadeIn(delay: 1200.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 20),
                
                // Email
                TextFormField(
                  controller: _emailController,
                  enabled: _isEditing,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: 'Email *',
                    hintText: 'Enter your email address',
                    prefixIcon: Icon(Icons.email_outlined),
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return 'Email is required';
                    }
                    if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
                      return 'Please enter a valid email';
                    }
                    return null;
                  },
                ).animate().fadeIn(delay: 1400.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 32),
                
                _buildSectionHeader('Address', Icons.home_outlined, 1600),
                
                const SizedBox(height: 20),
                
                // Apartment Number
                TextFormField(
                  controller: _apartmentController,
                  enabled: _isEditing,
                  decoration: const InputDecoration(
                    labelText: 'Apartment Number *',
                    hintText: 'e.g., 101, 2B',
                    prefixIcon: Icon(Icons.apartment),
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return 'Apartment number is required';
                    }
                    return null;
                  },
                ).animate().fadeIn(delay: 1800.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 20),
                
                // Building Name
                TextFormField(
                  controller: _buildingController,
                  enabled: _isEditing,
                  decoration: const InputDecoration(
                    labelText: 'Building Name *',
                    hintText: 'e.g., Building A, Tower 1',
                    prefixIcon: Icon(Icons.business),
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return 'Building name is required';
                    }
                    return null;
                  },
                ).animate().fadeIn(delay: 2000.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 20),
                
                // Phone Number
                TextFormField(
                  controller: _phoneController,
                  enabled: _isEditing,
                  keyboardType: TextInputType.phone,
                  decoration: const InputDecoration(
                    labelText: 'Phone Number',
                    hintText: '+1 (555) 123-4567',
                    prefixIcon: Icon(Icons.phone_outlined),
                  ),
                ).animate().fadeIn(delay: 2200.ms, duration: 600.ms).slideY(begin: 0.3),
                
                const SizedBox(height: 40),
                
                // Save & Cancel Buttons
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: _isLoading ? null : () {
                          setState(() {
                            _isEditing = false;
                          });
                          _loadUserData(); // Reset to original values
                        },
                        child: const Text('Cancel'),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: ElevatedButton(
                        onPressed: _isLoading ? null : _saveProfile,
                        child: _isLoading
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                                ),
                              )
                            : const Text('Save Changes'),
                      ),
                    ),
                  ],
                ).animate().fadeIn(delay: 2400.ms, duration: 600.ms).slideY(begin: 0.3),
              ] else ...[
                // Read-only Profile Info
                _buildSectionHeader('Personal Information', Icons.person_outline, 800),
                
                const SizedBox(height: 20),
                
                _buildInfoRow('First Name', _firstNameController.text),
                _buildInfoRow('Last Name', _lastNameController.text),
                _buildInfoRow('Display Name', _displayNameController.text),
                _buildInfoRow('Email', _emailController.text),
                
                const SizedBox(height: 32),
                
                _buildSectionHeader('Address', Icons.home_outlined, 1200),
                
                const SizedBox(height: 20),
                
                _buildInfoRow('Apartment Number', _apartmentController.text),
                _buildInfoRow('Building Name', _buildingController.text),
                _buildInfoRow('Phone Number', _phoneController.text),
              ],
              
              const SizedBox(height: 40),
              
              // Error Message
              Consumer<AuthProvider>(
                builder: (context, authProvider, child) {
                  if (authProvider.error != null) {
                    return Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: Theme.of(context).colorScheme.errorContainer,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          Icon(
                            Icons.error_outline,
                            color: Theme.of(context).colorScheme.error,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              authProvider.error!,
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.error,
                              ),
                            ),
                          ),
                          IconButton(
                            icon: const Icon(Icons.close),
                            onPressed: () => authProvider.clearError(),
                            color: Theme.of(context).colorScheme.error,
                          ),
                        ],
                      ),
                    ).animate().fadeIn(duration: 300.ms).scale();
                  }
                  return const SizedBox.shrink();
                },
              ),
            ],
          ),
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

  Widget _buildInfoRow(String label, String value) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: Theme.of(context).colorScheme.outline.withOpacity(0.2),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Theme.of(context).colorScheme.onSurface.withOpacity(0.6),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value.isEmpty ? 'Not provided' : value,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
