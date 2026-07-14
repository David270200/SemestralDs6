import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { View, ActivityIndicator } from 'react-native';
import {
  useFonts,
  PlusJakartaSans_400Regular,
  PlusJakartaSans_500Medium,
  PlusJakartaSans_700Bold,
} from '@expo-google-fonts/plus-jakarta-sans';
import { theme } from './theme';
import { CatalogScreen } from './screens/CatalogScreen';

const Stack = createNativeStackNavigator();

export default function App() {
  let [fontsLoaded] = useFonts({
    PlusJakartaSans_400Regular,
    PlusJakartaSans_500Medium,
    PlusJakartaSans_700Bold,
  });

  if (!fontsLoaded) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color="#A16207" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerShown: false, // Ocultamos el header nativo para usar el nuestro en CatalogScreen
          contentStyle: { backgroundColor: theme.colors.background }
        }}
      >
        <Stack.Screen name="Catalog" component={CatalogScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
