import React from 'react';
import { View, Text, Image, TouchableOpacity, StyleSheet } from 'react-native';
import { ShoppingCart } from 'lucide-react-native';
import { theme } from '../theme';

interface ProductCardProps {
  title: string;
  price: number;
  imageUrl: string;
  onPress: () => void;
  onAddToCart: () => void;
}

export function ProductCard({ title, price, imageUrl, onPress, onAddToCart }: ProductCardProps) {
  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.8}>
      <Image source={{ uri: imageUrl }} style={styles.image} resizeMode="cover" />
      <View style={styles.content}>
        <Text style={styles.title} numberOfLines={1}>{title}</Text>
        <Text style={styles.price}>${price.toFixed(2)}</Text>
        <TouchableOpacity style={styles.addButton} onPress={onAddToCart} activeOpacity={0.7}>
          <ShoppingCart color={theme.colors.onPrimary} size={18} />
          <Text style={styles.addButtonText}>Agregar</Text>
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: theme.colors.background,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
    marginBottom: theme.spacing.md,
    ...theme.shadows.md,
    borderColor: theme.colors.border,
    borderWidth: 1,
  },
  image: {
    width: '100%',
    height: 180,
    backgroundColor: theme.colors.muted,
  },
  content: {
    padding: theme.spacing.md,
  },
  title: {
    fontFamily: theme.typography.fontFamily.medium,
    fontSize: theme.typography.sizes.base,
    color: theme.colors.primary,
    marginBottom: theme.spacing.xs,
  },
  price: {
    fontFamily: theme.typography.fontFamily.bold,
    fontSize: theme.typography.sizes.lg,
    color: theme.colors.accent,
    marginBottom: theme.spacing.md,
  },
  addButton: {
    backgroundColor: theme.colors.primary,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: theme.spacing.sm,
    borderRadius: theme.borderRadius.md,
    gap: theme.spacing.xs,
  },
  addButtonText: {
    color: theme.colors.onPrimary,
    fontFamily: theme.typography.fontFamily.medium,
    fontSize: theme.typography.sizes.sm,
  },
});
