/* eslint-disable no-setter-return */
'use strict';

import _ from 'lodash';
import debug from 'debug';
import * as inflection from 'inflection';

import ctx from '../utils/context.ts';
import ModelCursor from './ModelCursor';
import Db from '../core/db/db.ts';
import { DocumentCollection, EdgeCollection } from 'arangojs/collection';
import { Database } from 'arangojs';
import { AqlQuery } from 'arangojs/aql';

export default class Model<DataType> {
  connections = {};
  collectionName: string;
  log: debug.Debugger;
  db: Db | undefined;
  collection: (DocumentCollection<any> & EdgeCollection<any>) | undefined;
  rawDb: Database | undefined;

  constructor(options: { name: string; keyAttribute?: string }) {
    options = _.defaults({}, options, { keyAttribute: '_key' });
    this.collectionName = options.name;

    this.collectionName = inflection.pluralize(_.lowerFirst(options.name));
    this.log = debug(`db:${options.name}`);

    ctx.on(
      'ensure:db',
      function (db: Db) {
        if (db.arango !== undefined) {
          this.db = db;
          this.rawDb = db.arango;
        }
      }.bind(this),
    );
  }

  ensureCollection() {
    if (this.db === undefined) {
      throw Error('Database undefined!');
    }
    if (this.rawDb === undefined) {
      throw Error('Model rawDb undefined!');
    }
    this.collection = this.rawDb.collection(this.collectionName);
    return this.db.ensureCollection(this.collectionName);
  }

  async firstExample(data: any) {
    this.log('firstExample %o', data);
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    let d;
    try {
      d = await Promise.resolve(this.collection.firstExample(data));
    } catch (e) {
      d = null;
    }
    return this.parse(d);
  }

  async findById(id: string) {
    this.log('findById %o', id);
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    const docs = await Promise.resolve(this.collection.lookupByKeys([id.toString()]));
    if (docs.length > 0) {
      return this.parse(docs[0]);
    }
    return null;
  }

  async ensureById(id: string, data: DataType, options?: { ignoreUnknownAttributes?: boolean; isNew?: boolean }) {
    return this.findOneById(id).then(
      function (resp) {
        if (resp) {
          return [this.parse(resp), false];
        } else {
          const entry = new Entry<DataType>(data, {
            isNew: options && options.isNew ? options.isNew : true,
            ignoreUnknownAttributes: options && options.ignoreUnknownAttributes ? options.ignoreUnknownAttributes : true,
          });
          entry._id = id;

          return this.save(entry).then((i) => [i, true]);
        }
      }.bind(this),
    );
  }

  findOneById(id: string) {
    this.log('findById %o', id);
    return this.firstExample({ id: id });
  }

  async ensureBy(key: string, value: any, data: DataType, options?: { ignoreUnknownAttributes?: boolean; isNew?: boolean }) {
    data[key] = value;
    return this.findOneBy(key, value).then(
      function (resp) {
        if (resp) {
          return [this.parse(resp.data), false];
        } else {
          const entry = new Entry<DataType>(data, {
            isNew: options && options.isNew ? options.isNew : true,
            ignoreUnknownAttributes: options && options.ignoreUnknownAttributes ? options.ignoreUnknownAttributes : true,
          });
          return this.save(entry).then((i) => [i, true]);
        }
      }.bind(this),
    );
  }

  findOneBy(key: string, value: any) {
    this.log(`findBy${key} ${value}`);
    return this.firstExample({ [key]: value });
  }

  create(data: any, options?: { isNew: boolean }) {
    const entry = new Entry<DataType>(data, {
      isNew: options ? options.isNew : true,
      ignoreUnknownAttributes: true,
    });
    return this.save(entry);
  }

  bulkCreate(datas: any[]) {
    this.log('bulkCreate %o items', datas.length);
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    return this.collection.import(datas).then(() => {
      return datas.map((data) => this.parse(data));
    });
  }

  ensure(entry: Entry<DataType>) {
    return this.findOneBy('_key', entry._key).then((instance) => {
      if (instance) {
        instance.isStored = true;
        return instance;
      } else {
        return this.save(
          new Entry<DataType>(entry.data, {
            isNew: false,
            ignoreUnknownAttributes: true,
          }),
        );
      }
    });
  }

  parse(data: DataType & { _id: string; _key: string }): Entry<DataType> | null {
    if (data === null) {
      return null;
    }

    const entry = new Entry<DataType>(data, {
      isNew: false,
      ignoreUnknownAttributes: true,
    });
    entry._id = data._id;
    entry._key = data._key;

    return entry;
  }

  wrapCursor(rawCursor: any) {
    return new ModelCursor(this, rawCursor);
  }

  async cursor(query: AqlQuery) {
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    if (this.rawDb === undefined) {
      throw Error('RawDb undefined!');
    }
    if (!query) {
      const rawCursor = await Promise.resolve(this.collection.all());
      return this.wrapCursor(rawCursor);
    } else {
      const rawCursor_2 = await Promise.resolve(this.rawDb.query(query));
      return this.wrapCursor(rawCursor_2);
    }
  }

  async findAll() {
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    const cursor1 = await Promise.resolve(this.collection.all());
    const ds = await cursor1.all();
    return ds.map((d) => this.parse(d));
  }

  save(entry: Entry<DataType>) {
    this.log('save %o', entry.data);
    if (this.collection === undefined) {
      throw Error('Collection undefined!');
    }
    if (entry.isNew) {
      entry.justCreated = true;
      return Promise.resolve(this.collection.save(_.defaults({ _key: entry._key }, entry.data)))
        .catch(() => {
          entry.justCreated = false;
          entry.justUpdated = false;
          return this.ensure(entry);
        })
        .then(function (resp) {
          entry._key = resp._key;
          entry._id = resp._id;
          entry.isNew = false;

          return entry;
        });
    } else {
      entry.justUpdated = true;
      return Promise.resolve(
        this.collection.update({ _id: entry._id, _key: entry._key === undefined ? '' : entry._key }, entry.data as object),
      )
        .catch(() => {
          entry.justCreated = false;
          entry.justUpdated = false;
          return this.ensure(entry);
        })
        .then(function (resp) {
          entry._key = resp._key;
          entry._id = resp._id;
          entry.isNew = false;

          return entry;
        });
    }
  }
}

export class Entry<DataType> {
  data: DataType;
  _id: string | undefined;
  _key: string | undefined;
  isStored: boolean | undefined;
  isNew: boolean;
  justCreated: boolean | undefined;
  justUpdated: boolean | undefined;
  constructor(data: DataType, options: { isNew: boolean; ignoreUnknownAttributes: boolean }) {
    this.data = _.defaults({}, data);
    options = _.defaults({}, options, { isNew: true, ignoreUnknownAttributes: false });

    this.isNew = options.isNew;
  }
}
