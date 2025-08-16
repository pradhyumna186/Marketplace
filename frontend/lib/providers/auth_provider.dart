import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../services/auth_service.dart';

class AuthProvider extends ChangeNotifier {
  User? _user;
  bool _isLoading = false;
  String? _error;
  String? _accessToken;
  String? _refreshToken;

  User? get user => _user;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _accessToken != null && _accessToken!.isNotEmpty;
  bool get hasValidToken => _accessToken != null && _accessToken!.isNotEmpty;
  bool get hasStoredTokens => _accessToken != null && _refreshToken != null;
  bool get isTokenLoading => _isLoading;

  final AuthService _authService = AuthService();

  AuthProvider() {
    _loadTokens();
  }

  Future<void> _loadTokens() async {
    _setLoading(true); // Set loading state
    
    try {
      // Add timeout to prevent getting stuck
      final prefs = await SharedPreferences.getInstance().timeout(
        const Duration(seconds: 10),
        onTimeout: () {
          print('Token loading timed out, using default values');
          return SharedPreferences.getInstance();
        },
      );
      
      _accessToken = prefs.getString('accessToken');
      _refreshToken = prefs.getString('refreshToken');
      
      print('Loaded tokens from storage - Access: ${_accessToken?.isNotEmpty == true ? '${_accessToken!.substring(0, 10)}...' : 'EMPTY'}');
      print('Loaded tokens from storage - Refresh: ${_refreshToken?.isNotEmpty == true ? '${_refreshToken!.substring(0, 10)}...' : 'EMPTY'}');
      
      // If we have tokens, try to load user data from storage and restore session
      if (_accessToken != null && _refreshToken != null) {
        await _loadUser();
        // Try to restore session from API to get fresh data
        await restoreSession();
      }
    } catch (e) {
      print('Error loading tokens: $e');
      // Set default values on error
      _accessToken = null;
      _refreshToken = null;
    } finally {
      // Always notify listeners when tokens are loaded, regardless of whether they exist
      print('Token loading complete - notifying listeners');
      _setLoading(false); // Clear loading state
      notifyListeners();
    }
  }



  Future<void> _saveTokens(String accessToken, String refreshToken) async {
    print('Saving tokens - Access: ${accessToken.isNotEmpty ? '${accessToken.substring(0, 10)}...' : 'EMPTY'}');
    print('Saving tokens - Refresh: ${refreshToken.isNotEmpty ? '${refreshToken.substring(0, 10)}...' : 'EMPTY'}');
    
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('accessToken', accessToken);
    await prefs.setString('refreshToken', refreshToken);
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    
    print('Tokens saved to memory - Access: ${_accessToken?.isNotEmpty == true ? '${_accessToken!.substring(0, 10)}...' : 'EMPTY'}');
  }

  Future<void> _saveUser(User user) async {
    final prefs = await SharedPreferences.getInstance();
    final userJson = user.toJson();
    final userDataString = jsonEncode(userJson);
    await prefs.setString('userData', userDataString);
    _user = user;
    print('User data saved to storage: ${user.username}');
  }

  Future<void> _loadUser() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final userDataString = prefs.getString('userData');
      if (userDataString != null) {
        final userData = jsonDecode(userDataString) as Map<String, dynamic>;
        _user = User.fromJson(userData);
        print('User data loaded from storage: ${_user?.username}');
      }
    } catch (e) {
      print('Error loading user data: $e');
    }
  }

  Future<void> _clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('accessToken');
    await prefs.remove('refreshToken');
    await prefs.remove('userData');
    _accessToken = null;
    _refreshToken = null;
    _user = null;
  }

  Future<bool> register({
    required String firstName,
    required String lastName,
    required String username,
    required String email,
    required String password,
    required String apartmentNumber,
    required String buildingName,
    String? phoneNumber,
    required bool acceptTerms,
  }) async {
    _setLoading(true);
    clearError();

    try {
      final response = await _authService.register(
        firstName: firstName,
        lastName: lastName,
        username: username,
        email: email,
        password: password,
        apartmentNumber: apartmentNumber,
        buildingName: buildingName,
        phoneNumber: phoneNumber,
        acceptTerms: acceptTerms,
      );

      if (response.success) {
        // Registration successful, user needs to verify email
        _setLoading(false);
        return true;
      } else {
        _setError(response.message ?? 'Registration failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      _setError('Registration failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> verifyEmail(String token) async {
    _setLoading(true);
    clearError();
    
    print('AuthProvider: Starting email verification for token: ${token.substring(0, 10)}...');

    try {
      final response = await _authService.verifyEmail(token);
      print('AuthProvider: Email verification response - success: ${response.success}, message: ${response.message}');

      if (response.success) {
        print('AuthProvider: Email verification successful');
        _setLoading(false);
        return true;
      } else {
        print('AuthProvider: Email verification failed - ${response.message}');
        _setError(response.message ?? 'Email verification failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      print('AuthProvider: Email verification error - $e');
      _setError('Email verification failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> login(String username, String password, {bool rememberDevice = false}) async {
    _setLoading(true);
    clearError();

    try {
      final response = await _authService.login(
        username: username,
        password: password,
        rememberDevice: rememberDevice,
      );

      if (response.success && response.accessToken != null && response.refreshToken != null) {
        await _saveTokens(response.accessToken!, response.refreshToken!);
        
        // Set user data from login response and save to storage
        if (response.user != null) {
          await _saveUser(response.user!);
          notifyListeners();
        }
        
        // Small delay to ensure tokens are fully stored
        await Future.delayed(const Duration(milliseconds: 100));
        
        _setLoading(false);
        return true;
      } else {
        _setError(response.message ?? 'Login failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      _setError('Login failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> updateProfile({
    String? firstName,
    String? lastName,
    String? displayName,
    String? email,
    String? apartmentNumber,
    String? buildingName,
    String? phoneNumber,
  }) async {
    _setLoading(true);
    clearError();

    try {
      if (!hasValidToken) {
        _setError('No valid access token available');
        _setLoading(false);
        return false;
      }
      
      final response = await _authService.updateProfile(
        firstName: firstName,
        lastName: lastName,
        displayName: displayName,
        email: email,
        apartmentNumber: apartmentNumber,
        buildingName: buildingName,
        phoneNumber: phoneNumber,
        accessToken: _accessToken!,
      );

      if (response.success) {
        // Update local user data
        if (_user != null) {
          _user = _user!.copyWith(
            firstName: firstName ?? _user!.firstName,
            lastName: lastName ?? _user!.lastName,
            displayName: displayName ?? _user!.displayName,
            email: email ?? _user!.email,
            apartmentNumber: apartmentNumber ?? _user!.apartmentNumber,
            buildingName: buildingName ?? _user!.buildingName,
            phoneNumber: phoneNumber ?? _user!.phoneNumber,
          );
        }
        
        _setLoading(false);
        notifyListeners();
        return true;
      } else {
        _setError(response.message ?? 'Profile update failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      _setError('Profile update failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> forgotPassword(String email) async {
    _setLoading(true);
    clearError();

    try {
      final response = await _authService.forgotPassword(email);

      if (response.success) {
        _setLoading(false);
        return true;
      } else {
        _setError(response.message ?? 'Password reset request failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      _setError('Password reset request failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> resetPassword(String token, String newPassword) async {
    _setLoading(true);
    clearError();

    try {
      final response = await _authService.resetPassword(token, newPassword);

      if (response.success) {
        _setLoading(false);
        return true;
      } else {
        _setError(response.message ?? 'Password reset failed');
        _setLoading(false);
        return false;
      }
    } catch (e) {
      _setError('Password reset failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<void> logout() async {
    try {
      if (_accessToken != null) {
        await _authService.logout(_accessToken!);
      }
    } catch (e) {
      // Ignore logout errors
    } finally {
      _user = null;
      await _clearTokens();
      notifyListeners();
    }
  }

  Future<void> logoutAllDevices() async {
    try {
      if (_accessToken != null) {
        await _authService.logoutAllDevices(_accessToken!);
      }
    } catch (e) {
      // Ignore logout errors
    } finally {
      _user = null;
      await _clearTokens();
      notifyListeners();
    }
  }

  Future<bool> refreshToken() async {
    if (_refreshToken == null) return false;

    try {
      final response = await _authService.refreshToken(_refreshToken!);

      if (response.success && response.accessToken != null) {
        await _saveTokens(response.accessToken!, _refreshToken!);
        
        // Load user profile data with the new token
        await _loadUserProfile();
        
        notifyListeners();
        return true;
      } else {
        // Refresh failed, clear tokens
        await _clearTokens();
        _user = null;
        notifyListeners();
        return false;
      }
    } catch (e) {
      // Refresh failed, clear tokens
      await _clearTokens();
      _user = null;
      notifyListeners();
      return false;
    }
  }

  Future<void> _loadUserProfile() async {
    try {
      if (_accessToken != null) {
        final response = await _authService.getUserProfile(_accessToken!);
        if (response.success && response.user != null) {
          await _saveUser(response.user!);
          print('User profile loaded successfully: ${_user?.username}');
        } else {
          print('Failed to load user profile: ${response.message}');
        }
      }
    } catch (e) {
      print('Error loading user profile: $e');
    }
  }

  Future<void> _loadUserData() async {
    try {
      // TODO: Implement get user profile API call when needed
      // This method is now only used for token refresh scenarios
      // User data is set directly from login response
      notifyListeners();
    } catch (e) {
      _setError('Failed to load user data: ${e.toString()}');
    }
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }

  void _setError(String error) {
    _error = error;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }

  // Method to manually set user data (useful for testing or manual updates)
  void setUser(User user) {
    _user = user;
    notifyListeners();
  }

  // Method to restore user session from stored tokens
  Future<void> restoreSession() async {
    if (_accessToken != null && _refreshToken != null) {
      print('Restoring user session from stored tokens...');

      try {
        // Try to load user profile from API to get fresh data
        await _loadUserProfile();
        print('User session restored successfully');
      } catch (e) {
        print('Failed to restore user session: $e');
        // If API call fails, user data should already be loaded from storage
      }
    }
  }
}
