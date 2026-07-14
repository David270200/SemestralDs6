import React from 'react';
import { View, Text, FlatList, StyleSheet, SafeAreaView, StatusBar, TouchableOpacity } from 'react-native';
import { ProductCard } from '../components/ProductCard';
import { theme } from '../theme';
import { ShoppingBag } from 'lucide-react-native';

const MOCK_PRODUCTS = [
  {
    id: '1',
    title: 'Camiseta de Algodón Premium',
    price: 25.00,
    imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&q=80&w=600',
  },
  {
    id: '2',
    title: 'Pantalón Chino Clásico',
    price: 45.00,
    imageUrl: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&q=80&w=600',
  },
  {
    id: '3',
    title: 'Chaqueta de Mezclilla',
    price: 65.00,
    imageUrl: 'https://images.unsplash.com/photo-1495105787522-5334e3ffa0eb?auto=format&fit=crop&q=80&w=600',
  },
  {
    id: '4',
    title: 'Zapatillas Urbanas Blancas',
    price: 80.00,
    imageUrl: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&q=80&w=600',
  }
];

export function CatalogScreen({ navigation }: any) {
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor={theme.colors.background} />
      
      {/* Header Customizado */}
      <View style={styles.header}>
        <View>
          <Text style={styles.subtitle}>Colección de Temporada</Text>
          <Text style={styles.title}>Nuevos Llegadas</Text>
        </View>
        <TouchableOpacity style={styles.cartButton} activeOpacity={0.7}>
          <ShoppingBag color={theme.colors.primary} size={24} />
          <View style={styles.badge}>
            <Text style={styles.badgeText}>2</Text>
          </View>
        </TouchableOpacity>
      </View>

      <FlatList
        data={MOCK_PRODUCTS}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <ProductCard 
            title={item.title}
            price={item.price}
            imageUrl={item.imageUrl}
            onPress={() => console.log('Ir a detalle', item.id)}
            onAddToCart={() => console.log('Agregar a carrito', item.id)}
          />
        )}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.md,
  },
  subtitle: {
    fontFamily: theme.typography.fontFamily.medium,
    fontSize: theme.typography.sizes.sm,
    color: theme.colors.accent,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  title: {
    fontFamily: theme.typography.fontFamily.bold,
    fontSize: theme.typography.sizes.hero,
    color: theme.colors.primary,
    marginTop: theme.spacing.xs,
  },
  cartButton: {
    padding: theme.spacing.xs,
    position: 'relative',
  },
  badge: {
    position: 'absolute',
    top: 0,
    right: 0,
    backgroundColor: theme.colors.destructive,
    borderRadius: theme.borderRadius.full,
    minWidth: 18,
    height: 18,
    justifyContent: 'center',
    alignItems: 'center',
  },
  badgeText: {
    color: theme.colors.onPrimary,
    fontFamily: theme.typography.fontFamily.bold,
    fontSize: 10,
  },
  listContent: {
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.spacing.xxl,
    paddingTop: theme.spacing.md,
  }
});
