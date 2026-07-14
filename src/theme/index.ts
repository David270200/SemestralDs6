export const theme = {
  colors: {
    primary: '#171717',
    onPrimary: '#FFFFFF',
    secondary: '#404040',
    accent: '#A16207', // Gold
    background: '#FFFFFF',
    foreground: '#171717',
    muted: '#E8ECF0',
    border: '#E5E5E5',
    destructive: '#DC2626',
    ring: '#171717',
  },
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    xxl: 48,
    xxxl: 64,
  },
  typography: {
    fontFamily: {
      regular: 'PlusJakartaSans_400Regular',
      medium: 'PlusJakartaSans_500Medium',
      bold: 'PlusJakartaSans_700Bold',
    },
    sizes: {
      xs: 12,
      sm: 14,
      base: 16,
      lg: 18,
      xl: 20,
      xxl: 24,
      hero: 32,
    }
  },
  shadows: {
    sm: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 1 },
      shadowOpacity: 0.05,
      shadowRadius: 2,
      elevation: 2,
    },
    md: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.1,
      shadowRadius: 6,
      elevation: 4,
    }
  },
  borderRadius: {
    sm: 4,
    md: 8,
    lg: 12,
    xl: 16,
    full: 9999,
  }
};
