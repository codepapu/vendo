import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

export default function AboutScreen() {
  const router = useRouter();

  const values = [
    {
      icon: 'target' as const,
      title: 'Customer First',
      description: 'Every decision we make prioritizes our customers\' needs and satisfaction.',
    },
    {
      icon: 'shield-checkmark' as const,
      title: 'Trust & Security',
      description: 'We ensure secure transactions and protect your personal information.',
    },
    {
      icon: 'trophy' as const,
      title: 'Quality',
      description: 'We partner only with verified retailers who meet our quality standards.',
    },
    {
      icon: 'heart' as const,
      title: 'Community',
      description: 'We build lasting relationships with customers, retailers, and partners.',
    },
  ];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>About Vendo</Text>
        <Text style={styles.subtitle}>
          Your trusted marketplace for quality products and exceptional service
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Our Story</Text>
        <Text style={styles.text}>
          Vendo was founded with a simple mission: to create a marketplace that connects
          customers with quality products from trusted retailers. We believe in making
          shopping easy, secure, and enjoyable for everyone.
        </Text>
        <Text style={styles.text}>
          Since our inception, we've grown into a platform that serves thousands of
          customers and hundreds of retailers, all while maintaining our commitment to
          excellence, transparency, and customer satisfaction.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Our Values</Text>
        <Text style={styles.sectionSubtitle}>The principles that guide everything we do</Text>
        {values.map((value, index) => (
          <View key={index} style={styles.valueCard}>
            <Ionicons name={value.icon} size={24} color="#3b82f6" />
            <View style={styles.valueContent}>
              <Text style={styles.valueTitle}>{value.title}</Text>
              <Text style={styles.valueDescription}>{value.description}</Text>
            </View>
          </View>
        ))}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Our Mission</Text>
        <Text style={styles.text}>
          To revolutionize online shopping by providing a seamless, secure, and enjoyable
          marketplace experience that benefits both customers and retailers.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Join Our Community</Text>
        <Text style={styles.text}>
          Whether you're a customer looking for great products or a retailer wanting to
          expand your reach, we welcome you to be part of the Vendo community.
        </Text>
        <TouchableOpacity
          style={styles.button}
          onPress={() => router.push('/register')}
        >
          <Text style={styles.buttonText}>Start Shopping</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.button, styles.buttonOutline]}
          onPress={() => router.push('/retailer')}
        >
          <Text style={styles.buttonOutlineText}>Become a Retailer</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    padding: 20,
    backgroundColor: '#f3f4f6',
    alignItems: 'center',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  section: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 12,
  },
  sectionSubtitle: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 16,
  },
  text: {
    fontSize: 16,
    color: '#4b5563',
    lineHeight: 24,
    marginBottom: 12,
  },
  valueCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
    padding: 12,
    backgroundColor: '#f9fafb',
    borderRadius: 8,
  },
  valueContent: {
    flex: 1,
    marginLeft: 12,
  },
  valueTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1f2937',
    marginBottom: 4,
  },
  valueDescription: {
    fontSize: 14,
    color: '#6b7280',
    lineHeight: 20,
  },
  button: {
    backgroundColor: '#3b82f6',
    padding: 14,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 12,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  buttonOutline: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: '#3b82f6',
    marginTop: 8,
  },
  buttonOutlineText: {
    color: '#3b82f6',
    fontSize: 16,
    fontWeight: '600',
  },
});
