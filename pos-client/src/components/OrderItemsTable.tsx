import React from 'react';
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { OrderItemData } from '../types/order.types';

interface Props {
  items: OrderItemData[];
}

/**
 * Shared table for displaying order line items (product + SKU details).
 * Can be reused on the product page when needed.
 */
export const OrderItemsTable: React.FC<Props> = ({ items }) => {
  if (!items || items.length === 0) {
    return (
      <Box p={2}>
        <Typography variant="body2" color="text.secondary">
          No items found for this order.
        </Typography>
      </Box>
    );
  }

  return (
    <Box p={2}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>SKU / Barcode</TableCell>
            <TableCell>Product</TableCell>
            <TableCell align="right">Quantity</TableCell>
            <TableCell align="right">MRP</TableCell>
            <TableCell align="right">Line Total</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => (
            <TableRow key={item.id}>
              <TableCell>{item.barcode}</TableCell>
              <TableCell>{item.productName}</TableCell>
              <TableCell align="right">{item.quantity}</TableCell>
              <TableCell align="right">₹{item.mrp.toFixed(2)}</TableCell>
              <TableCell align="right">₹{item.lineTotal.toFixed(2)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  );
};

