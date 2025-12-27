import { View, Text, ScrollView, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function TermsScreen() {
  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Ionicons name="scale" size={48} color="#3b82f6" />
        <Text style={styles.title}>Terms of Service</Text>
        <Text style={styles.date}>
          Last updated: {new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Agreement to Terms</Text>
        <Text style={styles.text}>
          By accessing or using Vendo's website and services, you agree to be bound by
          these Terms of Service and all applicable laws and regulations.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Account Registration</Text>
        <Text style={styles.text}>
          To access certain features, you may be required to register for an account.
          You agree to provide accurate information and maintain the security of your account.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Products and Pricing</Text>
        <Text style={styles.text}>
          We strive to provide accurate product descriptions and pricing. We reserve the
          right to correct errors and refuse or cancel any order at any time.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Payment Terms</Text>
        <Text style={styles.text}>
          All payments must be made through our approved payment methods. By making a
          purchase, you authorize us to charge your payment method for the total amount.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Returns and Refunds</Text>
        <Text style={styles.text}>
          Items must be returned within 30 days of purchase in original condition.
          Refunds will be processed to the original payment method.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Prohibited Uses</Text>
        <Text style={styles.text}>
          You may not use our service for any unlawful purpose, to violate regulations,
          infringe upon intellectual property rights, or submit false information.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Limitation of Liability</Text>
        <Text style={styles.text}>
          In no event shall Vendo be liable for any indirect, incidental, special,
          consequential, or punitive damages resulting from your use of the service.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Contact Information</Text>
        <Text style={styles.text}>
          If you have any questions about these Terms, please contact us at:
        </Text>
        <Text style={styles.text}>Email: legal@vendo.com</Text>
        <Text style={styles.text}>Address: 123 Commerce Street, Business City, BC 12345</Text>
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
    marginTop: 12,
    marginBottom: 8,
  },
  date: {
    fontSize: 14,
    color: '#6b7280',
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
  text: {
    fontSize: 16,
    color: '#4b5563',
    lineHeight: 24,
    marginBottom: 12,
  },
});

