import { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TextInput, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface FAQItem {
  question: string;
  answer: string;
  category: string;
}

const faqData: FAQItem[] = [
  {
    category: 'General',
    question: 'What is Vendo?',
    answer: 'Vendo is an online marketplace that connects customers with quality products from trusted retailers.',
  },
  {
    category: 'General',
    question: 'How do I create an account?',
    answer: 'Click on the "Register" button, fill in your information, and verify your email address.',
  },
  {
    category: 'Orders',
    question: 'How do I place an order?',
    answer: 'Browse products, add items to your cart, and proceed to checkout with your shipping and payment information.',
  },
  {
    category: 'Orders',
    question: 'How long does shipping take?',
    answer: 'Standard shipping typically takes 3-7 business days. Express shipping options are available at checkout.',
  },
  {
    category: 'Payments',
    question: 'What payment methods do you accept?',
    answer: 'We accept all major credit cards, debit cards, PayPal, and other secure payment methods.',
  },
  {
    category: 'Returns',
    question: 'What is your return policy?',
    answer: 'You can return most items within 30 days of purchase, provided they are in original condition.',
  },
];

export default function FAQScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [openItems, setOpenItems] = useState<number[]>([]);

  const filteredFAQs = faqData.filter(
    (item) =>
      item.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const toggleItem = (index: number) => {
    setOpenItems((prev) =>
      prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index]
    );
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Ionicons name="help-circle" size={48} color="#3b82f6" />
        <Text style={styles.title}>Frequently Asked Questions</Text>
        <Text style={styles.subtitle}>Find answers to common questions about Vendo</Text>
      </View>

      <View style={styles.searchSection}>
        <View style={styles.searchContainer}>
          <Ionicons name="search" size={20} color="#6b7280" style={styles.searchIcon} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search for answers..."
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
        </View>
      </View>

      <View style={styles.section}>
        {filteredFAQs.length > 0 ? (
          filteredFAQs.map((faq, index) => {
            const isOpen = openItems.includes(index);
            return (
              <TouchableOpacity
                key={index}
                style={styles.faqCard}
                onPress={() => toggleItem(index)}
              >
                <View style={styles.faqHeader}>
                  <View style={styles.faqHeaderContent}>
                    <Text style={styles.faqCategory}>{faq.category}</Text>
                    <Text style={styles.faqQuestion}>{faq.question}</Text>
                  </View>
                  <Ionicons
                    name={isOpen ? 'chevron-up' : 'chevron-down'}
                    size={24}
                    color="#6b7280"
                  />
                </View>
                {isOpen && <Text style={styles.faqAnswer}>{faq.answer}</Text>}
              </TouchableOpacity>
            );
          })
        ) : (
          <View style={styles.emptyState}>
            <Text style={styles.emptyText}>
              No FAQs found matching your search.
            </Text>
          </View>
        )}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Still Need Help?</Text>
        <Text style={styles.text}>
          Can't find the answer you're looking for? Our support team is here to help.
        </Text>
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            // Navigate to contact page
          }}
        >
          <Text style={styles.buttonText}>Contact Us</Text>
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
    marginTop: 12,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  searchSection: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 12,
    backgroundColor: '#fff',
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    paddingVertical: 12,
  },
  section: {
    padding: 20,
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
  faqCard: {
    backgroundColor: '#f9fafb',
    borderRadius: 8,
    padding: 16,
    marginBottom: 12,
  },
  faqHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  faqHeaderContent: {
    flex: 1,
    marginRight: 12,
  },
  faqCategory: {
    fontSize: 12,
    fontWeight: '600',
    color: '#3b82f6',
    backgroundColor: '#dbeafe',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
    alignSelf: 'flex-start',
    marginBottom: 8,
  },
  faqQuestion: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1f2937',
  },
  faqAnswer: {
    fontSize: 14,
    color: '#4b5563',
    lineHeight: 20,
    marginTop: 12,
  },
  emptyState: {
    padding: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
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
});

